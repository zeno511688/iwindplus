/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.operation.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.databind.JavaType;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.support.SupplierThrowable;
import com.iwindplus.base.redis.domain.constant.RedisConstant;
import com.iwindplus.base.redis.domain.dto.IdempotentTtlDTO;
import com.iwindplus.base.redis.domain.enums.IdempotentResultModeEnum;
import com.iwindplus.base.redis.domain.property.RedisProperty;
import com.iwindplus.base.redis.operation.RedissonIdempotentOperation;
import com.iwindplus.base.util.JacksonUtil;
import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.ScheduledDtpExecutor;
import org.redisson.api.RMap;
import org.redisson.api.RMapReactive;
import org.redisson.api.RScript;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * redis幂等操作实现类.
 *
 * @author zengdegui
 * @since 2026/04/04 11:09
 */
@Slf4j
public class RedissonIdempotentOperationImpl implements RedissonIdempotentOperation {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisProperty property;

    @Autowired(required = false)
    private ScheduledDtpExecutor idempotentTaskScheduler;

    private static final long SINGLE_FLIGHT_TIMEOUT_MS = 5000L;
    private static final long HEARTBEAT_THRESHOLD_MS = 60_000L;
    private static final long MIN_HEARTBEAT_PERIOD_MS = 2000L;
    private static final long MAX_HEARTBEAT_PERIOD_MS = 30000L;

    private static final long CACHE_MAX_SIZE = 10_000L;

    private static final Object EMPTY_SIGNAL = new Object();

    private static final String PUB_SUB_MSG_DONE = "done";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_DATA = "data";
    private static final String FIELD_HAS_DATA = "hasData";
    private static final String FIELD_VERSION = "version";

    private static final Set<String> DIRECT_FIELDS = Set.of(FIELD_STATUS, FIELD_HAS_DATA, FIELD_DATA);

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_FAILED = "FAILED";
    private static final String HAS_DATA_TRUE = "1";
    private static final String HAS_DATA_FALSE = "0";

    private static final int ACQUIRE_OK = 0;
    private static final int ACQUIRE_PROCESSING = 1;
    private static final int ACQUIRE_SUCCESS = 2;
    private static final int ACQUIRE_DUP = -1;
    private static final int ACQUIRE_INVALID = -2;

    private final Cache<String, CompletableFuture<Object>> cache =
        Caffeine.newBuilder()
            .maximumSize(CACHE_MAX_SIZE)
            .build();

    private static final HeartbeatHandle NO_OP_HEARTBEAT = () -> {
    };

    private static final String LUA_ACQUIRE = """ 
        local hasReq = (#KEYS == 2)
        local reqKey = hasReq and KEYS[1] or nil
        local bizKey = hasReq and KEYS[2] or KEYS[1]

        if hasReq then
            local val = redis.call('get', reqKey)
            if val then
                if val ~= bizKey then return {-2} end
            end
        end

        local status = redis.call('hget', bizKey, 'status')

        if not status then
            redis.call('hset', bizKey,
                'status','PROCESSING',
                'version',ARGV[3],
                'expireAt',ARGV[1]+ARGV[2]
            )
            redis.call('pexpire', bizKey, ARGV[2])

            if hasReq then
                redis.call('set', reqKey, bizKey, 'PX', ARGV[4])
            end

            return {0}
        end

        if status == 'SUCCESS' then
            local fields = redis.call('hmget', bizKey, 'data', 'hasData')
            return {2, fields[1], fields[2]}
        end

        if status == 'FAILED' then
            redis.call('hset', bizKey,
                'status','PROCESSING',
                'version',ARGV[3],
                'expireAt',ARGV[1]+ARGV[2]
            )
            redis.call('pexpire', bizKey, ARGV[2])

            if hasReq then
                redis.call('set', reqKey, bizKey, 'PX', ARGV[4])
            end

            return {0}
        end

        local expireAt = tonumber(redis.call('hget', bizKey, 'expireAt') or '0')

        if expireAt < tonumber(ARGV[1]) then
            redis.call('hset', bizKey,
                'version',ARGV[3],
                'expireAt',ARGV[1]+ARGV[2]
            )
            redis.call('pexpire', bizKey, ARGV[2])
            return {0}
        end

        return {1}
        """;

    private static final String LUA_COMMIT = """ 
        local version = redis.call('hget', KEYS[1], 'version')

        if version ~= ARGV[1] then
            return 0
        end

        redis.call('hset', KEYS[1],
            'status','SUCCESS',
            'data',ARGV[2],
            'hasData',ARGV[3]
        )

        redis.call('pexpire', KEYS[1], ARGV[4])

        return 1
        """;

    private static final String LUA_HEARTBEAT = """ 
        local version = redis.call('hget', KEYS[1], 'version')

        if version ~= ARGV[1] then
            return 0
        end

        local status = redis.call('hget', KEYS[1], 'status')
        if status ~= 'PROCESSING' then
            return 0
        end

        redis.call('hset', KEYS[1], 'expireAt', ARGV[2])
        redis.call('pexpire', KEYS[1], ARGV[3])

        return 1
        """;

    private static final String LUA_MARK_FAILED = """ 
        local version = redis.call('hget', KEYS[1], 'version')
                
        if version == ARGV[1] then
            redis.call('hset', KEYS[1],
                'status','FAILED',
                'expireAt',ARGV[2]
            )
            redis.call('pexpire', KEYS[1], ARGV[3])
            return 1
        end
                
        return 0
        """;

    @Override
    public <T> T execute(
        String reqKey,
        String bizKey,
        Duration processingTtl,
        Duration successTtl,
        JavaType javaType,
        IdempotentResultModeEnum resultMode,
        SupplierThrowable<T> supplier) {

        validateParams(bizKey, supplier);

        final IdempotentTtlDTO ttl = buildTtl(processingTtl, successTtl);
        final long timeout = Math.max(ttl.getProcessingTtl().toMillis(), SINGLE_FLIGHT_TIMEOUT_MS);

        try {
            return singleFlightSync(bizKey,
                () -> doExecute(reqKey, bizKey, ttl.getProcessingTtl(), ttl.getSuccessTtl(),
                    javaType, resultMode, supplier),
                timeout);
        } catch (Exception e) {
            throw handleExecuteError(e, bizKey);
        }
    }

    @Override
    public <T> Mono<T> executeReactive(
        String reqKey,
        String bizKey,
        Duration processingTtl,
        Duration successTtl,
        JavaType javaType,
        IdempotentResultModeEnum resultMode,
        Supplier<Mono<T>> supplier) {

        validateParams(bizKey, supplier);

        final IdempotentTtlDTO ttl = buildTtl(processingTtl, successTtl);
        final long timeout = Math.max(ttl.getProcessingTtl().toMillis(), SINGLE_FLIGHT_TIMEOUT_MS);

        return singleFlightAsync(bizKey,
            () -> doExecuteReactive(reqKey, bizKey, ttl.getProcessingTtl(), ttl.getSuccessTtl(),
                javaType, resultMode, supplier),
            timeout);
    }

    private <T> T doExecute(
        String reqKey,
        String bizKey,
        Duration processingTtl,
        Duration successTtl,
        JavaType javaType,
        IdempotentResultModeEnum resultMode,
        SupplierThrowable<T> supplier) {

        long now = System.currentTimeMillis();
        String version = UUID.randomUUID().toString();

        AcquireResult r = evalAcquireSync(reqKey, bizKey, now, processingTtl.toMillis(), version, successTtl.toMillis());

        return switch (r.code()) {
            case ACQUIRE_OK -> {
                HeartbeatHandle hb = startHeartbeatSync(bizKey, version, processingTtl.toMillis());
                try {
                    T res;
                    try {
                        res = supplier.get();
                    } catch (Throwable e) {
                        markFailedSync(bizKey, version, processingTtl.toMillis());
                        throw handleExecuteError(e, bizKey);
                    }
                    commitSync(bizKey, version, res, successTtl.toMillis());
                    yield res;
                } finally {
                    hb.stop();
                }
            }
            case ACQUIRE_SUCCESS -> handleSuccess(r, javaType, resultMode);
            case ACQUIRE_PROCESSING -> handleWaitSync(bizKey, javaType, resultMode, processingTtl);
            case ACQUIRE_DUP -> throw new BizException(BizCodeEnum.IDEMPOTENT_REQUEST_DUPLICATE);
            case ACQUIRE_INVALID -> throw new BizException(BizCodeEnum.IDEMPOTENT_REQUEST_INVALID);
            default -> throw new BizException(BizCodeEnum.EXECUTE_ERROR);
        };
    }

    private <T> Mono<T> doExecuteReactive(
        String reqKey,
        String bizKey,
        Duration processingTtl,
        Duration successTtl,
        JavaType javaType,
        IdempotentResultModeEnum resultMode,
        Supplier<Mono<T>> supplier) {

        long now = System.currentTimeMillis();
        String version = UUID.randomUUID().toString();

        return evalAcquireReactive(reqKey, bizKey, now,
            processingTtl.toMillis(), version, successTtl.toMillis())
            .flatMap(r ->
                switch (r.code()) {
                    case ACQUIRE_OK -> {
                        HeartbeatHandle hb = startHeartbeatReactive(bizKey, version, processingTtl.toMillis());
                        yield supplier.get()
                            .onErrorResume(e ->
                                markFailedReactive(bizKey, version, processingTtl.toMillis())
                                    .then(Mono.error(handleExecuteError(e, bizKey)))
                            )
                            .flatMap(res ->
                                commitReactive(bizKey, version, res, successTtl.toMillis())
                                    .thenReturn(res)
                            )
                            .switchIfEmpty(Mono.defer(() ->
                                commitReactive(bizKey, version, null, successTtl.toMillis())
                                    .then(Mono.empty())
                            ))
                            .doFinally(s -> hb.stop());
                    }
                    case ACQUIRE_SUCCESS -> Mono.justOrEmpty(handleSuccess(r, javaType, resultMode));
                    case ACQUIRE_PROCESSING -> handleWaitReactive(bizKey, javaType, resultMode, processingTtl);
                    case ACQUIRE_DUP -> Mono.error(new BizException(BizCodeEnum.IDEMPOTENT_REQUEST_DUPLICATE));
                    case ACQUIRE_INVALID -> Mono.error(new BizException(BizCodeEnum.IDEMPOTENT_REQUEST_INVALID));
                    default -> Mono.error(new BizException(BizCodeEnum.EXECUTE_ERROR));
                }
            );
    }

    private <T> T singleFlightSync(String key, Callable<T> task, long timeout) throws Exception {

        CompletableFuture<Object> cf = new CompletableFuture<>();
        CompletableFuture<Object> prev = cache.asMap().putIfAbsent(key, cf);

        if (prev == null) {
            try {
                T r = task.call();
                cf.complete(r);
                return r;
            } catch (Throwable e) {
                if (!cf.isDone()) {
                    cf.completeExceptionally(e);
                }
                throw e;
            } finally {
                cache.invalidate(key);
            }
        }

        try {
            return (T) prev.get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            throw handleExecuteError(cause, key);
        } catch (TimeoutException e) {
            log.warn("singleFlight timeout bizKey={}", key);
            throw new BizException(BizCodeEnum.IDEMPOTENT_EXECUTE_TIMEOUT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(BizCodeEnum.EXECUTE_ERROR);
        }
    }

    private <T> Mono<T> singleFlightAsync(
        String key,
        Supplier<Mono<T>> task,
        long timeout) {

        return Mono.defer(() -> {
            CompletableFuture<Object> cf = new CompletableFuture<>();
            CompletableFuture<Object> prev = cache.asMap().putIfAbsent(key, cf);

            if (prev != null) {
                return Mono.fromFuture(prev)
                    .flatMap(v -> v == EMPTY_SIGNAL ? Mono.empty() : Mono.just((T) v))
                    .timeout(Duration.ofMillis(timeout))
                    .onErrorMap(TimeoutException.class, e -> new BizException(BizCodeEnum.IDEMPOTENT_EXECUTE_TIMEOUT));
            }

            return Mono.defer(task)
                .map(v -> (Object) v)
                .defaultIfEmpty(EMPTY_SIGNAL)
                .doOnNext(v -> {
                    if (!cf.isDone()) {
                        cf.complete(v);
                    }
                })
                .doOnError(e -> {
                    if (!cf.isDone()) {
                        cf.completeExceptionally(e);
                    }
                })
                .doFinally(sig -> cache.invalidate(key))
                .flatMap(v -> v == EMPTY_SIGNAL ? Mono.empty() : Mono.just((T) v));
        });
    }

    private <T> T handleSuccess(AcquireResult r, JavaType type, IdempotentResultModeEnum resultMode) {
        if (resultMode == IdempotentResultModeEnum.THROW_ERROR) {
            throw new BizException(BizCodeEnum.IDEMPOTENT_BIZ_DUPLICATE);
        }
        if (!HAS_DATA_TRUE.equals(r.hasData())) {
            return null;
        }
        return JacksonUtil.parseObject(r.data(), type);
    }

    private <T> T handleWaitSync(String bizKey, JavaType type, IdempotentResultModeEnum resultMode, Duration processingTtl) {
        if (resultMode == IdempotentResultModeEnum.THROW_ERROR) {
            throw new BizException(BizCodeEnum.IDEMPOTENT_BIZ_DUPLICATE);
        }
        if (resultMode != IdempotentResultModeEnum.WAIT) {
            throw new BizException(BizCodeEnum.IDEMPOTENT_EXECUTE_PLEASE_WAIT, new Object[]{processingTtl.toSeconds()});
        }

        DirectResult<T> direct = getDirectSync(bizKey, type);
        if (direct != null) {
            return direct.value();
        }

        long timeout = processingTtl.toMillis();
        return waitSync(bizKey, type, timeout);
    }

    private <T> Mono<T> handleWaitReactive(String bizKey, JavaType type, IdempotentResultModeEnum resultMode, Duration processingTtl) {
        if (resultMode == IdempotentResultModeEnum.THROW_ERROR) {
            throw new BizException(BizCodeEnum.IDEMPOTENT_BIZ_DUPLICATE);
        }
        if (resultMode != IdempotentResultModeEnum.WAIT) {
            throw new BizException(BizCodeEnum.IDEMPOTENT_EXECUTE_PLEASE_WAIT, new Object[]{processingTtl.toSeconds()});
        }

        long timeout = processingTtl.toMillis();

        return this.<T>getDirectReactive(bizKey, type)
            .switchIfEmpty(waitReactive(bizKey, type, timeout))
            .flatMap(v -> Mono.justOrEmpty(v.value()));
    }

    private void markFailedSync(String key, String version, long ttl) {
        try {
            redissonClient.getScript(StringCodec.INSTANCE).eval(
                RScript.Mode.READ_WRITE,
                LUA_MARK_FAILED,
                RScript.ReturnType.INTEGER,
                List.of(key),
                version,
                System.currentTimeMillis() + ttl,
                ttl
            );
        } catch (Exception e) {
            log.warn("failed to mark key={} as failed", key, e);
        }
    }

    private Mono<Object> markFailedReactive(String key, String version, long ttl) {
        return redissonClient.reactive()
            .getScript(StringCodec.INSTANCE)
            .eval(RScript.Mode.READ_WRITE, LUA_MARK_FAILED, RScript.ReturnType.INTEGER,
                List.of(key),
                version,
                System.currentTimeMillis() + ttl,
                ttl
            )
            .onErrorResume(e -> {
                log.warn("failed to mark key={} as failed", key, e);
                return Mono.just(0);
            });
    }

    private AcquireResult evalAcquireSync(String reqKey, String bizKey, long now, long ttl, String version, long reqTtl) {
        Object res = redissonClient.getScript(StringCodec.INSTANCE).eval(
            RScript.Mode.READ_WRITE,
            LUA_ACQUIRE,
            RScript.ReturnType.MULTI,
            getKeys(reqKey, bizKey),
            now, ttl, version, reqTtl
        );
        return toResult(res);
    }

    private Mono<AcquireResult> evalAcquireReactive(String reqKey, String bizKey, long now, long ttl, String version, long reqTtl) {
        return redissonClient.reactive()
            .getScript(StringCodec.INSTANCE)
            .eval(RScript.Mode.READ_WRITE, LUA_ACQUIRE,
                RScript.ReturnType.MULTI,
                getKeys(reqKey, bizKey),
                now, ttl, version, reqTtl)
            .map(this::toResult)
            .switchIfEmpty(Mono.error(new BizException(BizCodeEnum.EXECUTE_ERROR)));
    }

    private <T> T waitSync(String bizKey, JavaType type, long timeout) {
        String channel = RedisConstant.IDEMPOTENT_CHANNEL_KEY_PREFIX + bizKey;
        RTopic topic = redissonClient.getTopic(channel);

        final BlockingQueue<DirectResult<T>> queue = new ArrayBlockingQueue<>(1);

        int listenerId = topic.addListener(String.class, (c, m) -> {
            try {
                DirectResult<T> v = getDirectSync(bizKey, type);
                if (v != null) {
                    queue.clear();
                    queue.offer(v);
                }
            } catch (Exception e) {
                log.warn("subscribeSync failed bizKey={}", bizKey, e);
            }
        });

        ScheduledFuture<?> poll = null;

        if (idempotentTaskScheduler != null) {
            poll = idempotentTaskScheduler.scheduleAtFixedRate(() -> {
                try {
                    DirectResult<T> v = getDirectSync(bizKey, type);
                    if (v != null) {
                        queue.clear();
                        queue.offer(v);
                    }
                } catch (Exception e) {
                    log.warn("polling failed bizKey={}", bizKey, e);
                }
            }, 100, 100, TimeUnit.MILLISECONDS);
        }

        try {
            DirectResult<T> result = queue.poll(timeout, TimeUnit.MILLISECONDS);
            if (result != null) {
                return result.value();
            }

            DirectResult<T> fallback = getDirectSync(bizKey, type);
            if (fallback != null) {
                return fallback.value();
            }

            throw new BizException(BizCodeEnum.IDEMPOTENT_EXECUTE_TIMEOUT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(BizCodeEnum.EXECUTE_ERROR);
        } finally {
            topic.removeListener(listenerId);
            if (poll != null) {
                poll.cancel(false);
            }
        }
    }

    private <T> Mono<DirectResult<T>> waitReactive(String bizKey, JavaType type, long timeout) {
        String channel = RedisConstant.IDEMPOTENT_CHANNEL_KEY_PREFIX + bizKey;

        final Flux<DirectResult<T>> pubsubFlux = Flux.<String>create(sink -> {
                RTopic topic = redissonClient.getTopic(channel);

                int listenerId = topic.addListener(String.class, (c, m) -> {
                    if (!sink.isCancelled()) {
                        sink.next(m);
                    }
                });

                sink.onDispose(() -> topic.removeListener(listenerId));
            }, FluxSink.OverflowStrategy.LATEST)
            .concatMap(m -> getDirectReactive(bizKey, type));

        long interval = Math.max(Math.min(timeout / 10, 200), 20);

        final Flux<DirectResult<T>> pollingFlux =
            Flux.interval(Duration.ofMillis(interval))
                .concatMap(i -> getDirectReactive(bizKey, type));

        return Flux.merge(pubsubFlux, pollingFlux)
            .filter(Objects::nonNull)
            .next()
            .timeout(Duration.ofMillis(timeout))
            .onErrorResume(TimeoutException.class, e ->
                this.<T>getDirectReactive(bizKey, type)
                    .switchIfEmpty(Mono.error(new BizException(BizCodeEnum.IDEMPOTENT_EXECUTE_TIMEOUT)))
            );
    }

    private <T> DirectResult<T> getDirectSync(String key, JavaType type) {
        RMap<String, String> map = redissonClient.getMap(key, StringCodec.INSTANCE);
        final Map<String, String> fields = map.getAll(DIRECT_FIELDS);

        if (!STATUS_SUCCESS.equals(fields.get(FIELD_STATUS))) {
            return null;
        }

        if (!HAS_DATA_TRUE.equals(fields.get(FIELD_HAS_DATA))) {
            return new DirectResult<>(null);
        }

        return new DirectResult<>(JacksonUtil.parseObject(fields.get(FIELD_DATA), type));
    }

    private <T> Mono<DirectResult<T>> getDirectReactive(String key, JavaType type) {
        RMapReactive<String, String> map = redissonClient.reactive()
            .getMap(key, StringCodec.INSTANCE);

        return map.getAll(DIRECT_FIELDS)
            .flatMap(fields -> {
                if (!STATUS_SUCCESS.equals(fields.get(FIELD_STATUS))) {
                    return Mono.empty();
                }

                if (!HAS_DATA_TRUE.equals(fields.get(FIELD_HAS_DATA))) {
                    return Mono.just(new DirectResult<>(null));
                }

                try {
                    T result = JacksonUtil.parseObject(fields.get(FIELD_DATA), type);
                    return Mono.just(new DirectResult<>(result));
                } catch (Exception e) {
                    log.warn("failed to parse result for key={}", key, e);
                    return Mono.empty();
                }
            });
    }

    private void commitSync(String key, String version, Object res, long ttl) {
        Long ok = redissonClient.getScript(StringCodec.INSTANCE).eval(
            RScript.Mode.READ_WRITE,
            LUA_COMMIT,
            RScript.ReturnType.VALUE,
            List.of(key),
            version,
            res == null ? "" : JacksonUtil.toJsonStr(res),
            res == null ? HAS_DATA_FALSE : HAS_DATA_TRUE,
            ttl
        );

        if (Objects.equals(ok, 1L)) {
            try {
                redissonClient.getTopic(RedisConstant.IDEMPOTENT_CHANNEL_KEY_PREFIX + key)
                    .publish(PUB_SUB_MSG_DONE);
            } catch (Exception e) {
                log.warn("failed to publish done message for key={}", key, e);
            }
        }
    }

    private <T> Mono<Void> commitReactive(String key, String version, T res, long ttl) {
        return redissonClient.reactive()
            .getScript(StringCodec.INSTANCE)
            .eval(RScript.Mode.READ_WRITE, LUA_COMMIT, RScript.ReturnType.VALUE,
                List.of(key),
                version,
                res == null ? "" : JacksonUtil.toJsonStr(res),
                res == null ? HAS_DATA_FALSE : HAS_DATA_TRUE,
                ttl
            )
            .flatMap(ok -> {
                if (Objects.equals(ok, 1L)) {
                    return redissonClient.reactive().getTopic(RedisConstant.IDEMPOTENT_CHANNEL_KEY_PREFIX + key)
                        .publish(PUB_SUB_MSG_DONE)
                        .then();
                }
                return Mono.empty();
            });
    }

    private HeartbeatHandle startHeartbeatSync(String key, String version, long ttl) {
        if (Boolean.FALSE.equals(property.getIdempotent().getEnabledHeartbeat())
            || ttl <= HEARTBEAT_THRESHOLD_MS
            || idempotentTaskScheduler == null) {
            return NO_OP_HEARTBEAT;
        }

        long period = calculateHeartbeatPeriod(ttl);
        AtomicBoolean stopped = new AtomicBoolean(false);

        ScheduledFuture<?> f = idempotentTaskScheduler.scheduleAtFixedRate(() -> {
            if (stopped.get()) {
                return;
            }

            try {
                Long r = redissonClient.getScript(StringCodec.INSTANCE).eval(
                    RScript.Mode.READ_WRITE,
                    LUA_HEARTBEAT,
                    RScript.ReturnType.INTEGER,
                    List.of(key),
                    version,
                    System.currentTimeMillis() + ttl,
                    ttl
                );

                if (Objects.equals(r, 0L)) {
                    stopped.set(true);
                    log.warn("heartbeat stopped key={}", key);
                }

            } catch (Exception e) {
                log.warn("heartbeat failed key={}", key, e);
            }

        }, period, period, TimeUnit.MILLISECONDS);

        return () -> {
            stopped.set(true);
            f.cancel(false);
        };
    }

    private HeartbeatHandle startHeartbeatReactive(String key, String version, long ttl) {
        if (Boolean.FALSE.equals(property.getIdempotent().getEnabledHeartbeat()) || ttl <= HEARTBEAT_THRESHOLD_MS) {
            return NO_OP_HEARTBEAT;
        }

        long period = calculateHeartbeatPeriod(ttl);

        Disposable disposable = Flux.interval(Duration.ofMillis(period))
            .concatMap(i ->
                redissonClient.reactive()
                    .getScript(StringCodec.INSTANCE)
                    .eval(
                        RScript.Mode.READ_WRITE,
                        LUA_HEARTBEAT,
                        RScript.ReturnType.INTEGER,
                        List.of(key),
                        version,
                        System.currentTimeMillis() + ttl,
                        ttl
                    )
                    .flatMap(r -> {
                        if (Objects.equals(r, 0L)) {
                            return Mono.error(new RuntimeException("heartbeat stop: key=" + key));
                        }
                        return Mono.just(r);
                    })
            )
            .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofMillis(period))
                .filter(e -> !(e instanceof RuntimeException re
                    && CharSequenceUtil.isNotBlank(re.getMessage())
                    && re.getMessage().startsWith("heartbeat stop")))
                .onRetryExhaustedThrow((spec, signal) -> signal.failure())
            )
            .onErrorComplete(e -> e instanceof RuntimeException re
                && CharSequenceUtil.isNotBlank(re.getMessage())
                && re.getMessage().startsWith("heartbeat stop"))
            .subscribe(null, e -> log.warn("sync heartbeat failed key={}", key, e));

        return disposable::dispose;
    }

    private AcquireResult toResult(Object obj) {
        if (!(obj instanceof List<?> list) || list.isEmpty()) {
            return new AcquireResult(Integer.MIN_VALUE, null, null);
        }

        int code = list.get(0) instanceof Number n ? n.intValue() : Integer.MIN_VALUE;
        Object rawData = list.size() > 1 ? list.get(1) : null;
        Object rawHasData = list.size() > 2 ? list.get(2) : null;

        String data = rawData != null ? rawData.toString() : null;
        String hasData = rawHasData != null ? rawHasData.toString() : null;
        return new AcquireResult(code, data, hasData);
    }

    private List<Object> getKeys(String reqKey, String bizKey) {
        return CharSequenceUtil.isBlank(reqKey)
            ? List.of(bizKey)
            : List.of(reqKey, bizKey);
    }

    private IdempotentTtlDTO buildTtl(Duration processingTtl, Duration successTtl) {
        RedisProperty.IdempotentConfig cfg = property.getIdempotent();
        return IdempotentTtlDTO.builder()
            .processingTtl(Optional.ofNullable(processingTtl).orElse(cfg.getProcessingTtl()))
            .successTtl(Optional.ofNullable(successTtl).orElse(cfg.getSuccessTtl()))
            .build();
    }

    private RuntimeException handleExecuteError(Throwable e, String bizKey) {
        if (e instanceof BizException bizEx) {
            return bizEx;
        }

        if (CharSequenceUtil.isNotBlank(bizKey)) {
            log.error("幂等执行异常 bizKey={}", bizKey, e);
        } else {
            log.error("幂等执行异常", e);
        }

        return new BizException(BizCodeEnum.EXECUTE_ERROR);
    }

    private void validateParams(String bizKey, Object supplier) {
        if (CharSequenceUtil.isBlank(bizKey)) {
            throw new BizException(BizCodeEnum.PARAM_ERROR);
        }
        if (supplier == null) {
            throw new BizException(BizCodeEnum.PARAM_ERROR);
        }
    }

    private long calculateHeartbeatPeriod(long ttl) {
        long period = ttl / 3;
        return Math.min(Math.max(period, MIN_HEARTBEAT_PERIOD_MS), MAX_HEARTBEAT_PERIOD_MS);
    }

    private record AcquireResult(int code, String data, String hasData) {

    }

    private record DirectResult<T>(T value) {

    }

    private interface HeartbeatHandle {

        void stop();
    }
}
