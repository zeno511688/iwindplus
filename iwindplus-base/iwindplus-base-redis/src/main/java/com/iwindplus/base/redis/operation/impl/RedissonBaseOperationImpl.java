/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.operation.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.redis.support.RedisKeyResolver;
import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.interceptor.KeyGenerator;
import reactor.core.publisher.Mono;

/**
 * redisson基本操作实现类.
 *
 * @author zengdegui
 * @since 2026/04/04 11:09
 */
@Slf4j
public class RedissonBaseOperationImpl extends AbstractRedissonBaseOperationImpl {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private CacheProperties cacheProperties;

    @Override
    public String getKeyPrefix() {
        return Optional.ofNullable(cacheProperties.getRedis())
            .filter(CacheProperties.Redis::isUseKeyPrefix)
            .map(CacheProperties.Redis::getKeyPrefix)
            .orElse(null);
    }

    @Override
    public String getRedisKey(
        String bizKey,
        String[] names,
        RedisKeyResolver keyResolver,
        JoinPoint joinPoint,
        KeyGenerator keyGenerator,
        String[] keys) {

        final String profile = Optional.ofNullable(SpringUtil.getActiveProfile())
            .orElse("default");

        final String keyPrefix = this.getKeyPrefix();

        final StringBuilder sb = new StringBuilder();
        sb.append(profile).append(SymbolConstant.COLON);

        if (CharSequenceUtil.isNotBlank(keyPrefix)) {
            sb.append(keyPrefix).append(SymbolConstant.COLON);
        }

        if (CharSequenceUtil.isNotBlank(bizKey) && bizKey.endsWith(SymbolConstant.COLON)) {
            bizKey = bizKey.substring(0, bizKey.length() - 1);
        }

        sb.append(bizKey);

        if (ArrayUtil.isNotEmpty(names)) {
            sb.append(SymbolConstant.COLON).append(String.join(SymbolConstant.UNDERLINE, names));
        }

        String resolver = null;

        if (keyResolver != null && joinPoint != null && keyGenerator != null) {
            resolver = keyResolver.resolver(joinPoint, keyGenerator, keys);
        }

        if (CharSequenceUtil.isNotBlank(resolver)) {
            sb.append(SymbolConstant.COLON).append(resolver);
        }

        return sb.toString();
    }

    @Override
    public void set(String key, String value) {
        execute(() -> {
            redissonClient.getBucket(key).set(value);
            return null;
        });
    }

    @Override
    public void set(String key, String value, Duration duration) {
        execute(() -> {
            redissonClient.getBucket(key).set(value, duration);
            return null;
        });
    }

    @Override
    public boolean setIfAbsent(String key, String value, Duration ttl) {
        return execute(() ->
            redissonClient.getBucket(key).setIfAbsent(value, ttl)
        );
    }

    @Override
    public Mono<Boolean> setIfAbsentMono(String key, String value, Duration ttl) {
        return executeReactive(() ->
            redissonClient.reactive().getBucket(key).setIfAbsent(value, ttl)
        );
    }

    @Override
    public Object get(String key) {
        return execute(() ->
            redissonClient.getBucket(key).get()
        );
    }

    @Override
    public <T> void hSet(String key, String hashKey, T value) {
        execute(() -> {
            redissonClient.getMap(key).put(hashKey, value);
            return null;
        });
    }

    @Override
    public <T> T hGet(String key, String hashKey) {
        return execute(() ->
            redissonClient.<String, T>getMap(key).get(hashKey)
        );
    }

    @Override
    public <T> Map<String, T> hGetAll(String key) {
        return execute(() ->
            redissonClient.<String, T>getMap(key).readAllMap()
        );
    }

    @Override
    public void lPush(String key, Object... values) {
        execute(() -> {
            RList<Object> list = redissonClient.getList(key);
            list.addAll(0, Arrays.stream(values).filter(Objects::nonNull).toList());
            return null;
        });
    }

    @Override
    public void rPush(String key, Object... values) {
        execute(() -> {
            redissonClient.getList(key)
                .addAll(Arrays.stream(values).filter(Objects::nonNull).toList());
            return null;
        });
    }

    @Override
    public Object lPop(String key) {
        return execute(() ->
            redissonClient.getDeque(key).poll()
        );
    }

    @Override
    public Object rPop(String key) {
        return execute(() ->
            redissonClient.getDeque(key).pollLast()
        );
    }

    @Override
    public List<Object> lRange(String key, int start, int end) {
        int finalEnd = end;
        return execute(() -> {
            RList<Object> list = redissonClient.getList(key);
            int size = list.size();

            if (size == 0 || start >= size || start > end) {
                return List.of();
            }

            return list.range(start, Math.min(finalEnd - 1, size - 1));
        });
    }

    @Override
    public void sAdd(String key, Object... values) {
        execute(() -> {
            redissonClient.getSet(key)
                .addAll(Arrays.stream(values).filter(Objects::nonNull).toList());
            return null;
        });
    }

    @Override
    public Set<Object> sMembers(String key) {
        return execute(() ->
            redissonClient.getSet(key).readAll()
        );
    }

    @Override
    public void sRem(String key, Object... values) {
        execute(() -> {
            redissonClient.getSet(key)
                .removeAll(Arrays.stream(values).filter(Objects::nonNull).toList());
            return null;
        });
    }

    @Override
    public void zAdd(String key, double score, Object value) {
        execute(() -> {
            redissonClient.getScoredSortedSet(key).add(score, value);
            return null;
        });
    }

    @Override
    public Collection<Object> zRange(String key, int start, int end) {
        return execute(() ->
            redissonClient.getScoredSortedSet(key).valueRange(start, end)
        );
    }

    @Override
    public void zRem(String key, Object... values) {
        execute(() -> {
            redissonClient.getScoredSortedSet(key)
                .removeAll(Arrays.stream(values).filter(Objects::nonNull).toList());
            return null;
        });
    }

    @Override
    public boolean delete(String key) {
        return execute(() ->
            redissonClient.getBucket(key).delete()
        );
    }

    @Override
    public boolean exists(String key) {
        return execute(() ->
            redissonClient.getBucket(key).isExists()
        );
    }

    @Override
    public boolean expire(String key, Duration duration) {
        return execute(() ->
            redissonClient.getBucket(key).expire(duration)
        );
    }

    @Override
    public long ttl(String key) {
        return execute(() ->
            redissonClient.getBucket(key).remainTimeToLive()
        );
    }

    @Override
    public RAtomicLong getAtomicCounter(String key) {
        return redissonClient.getAtomicLong(key);
    }

    @Override
    public long incrementAndGet(String key) {
        return execute(() ->
            getAtomicCounter(key).incrementAndGet()
        );
    }

    @Override
    public long incrementBy(String key, long delta) {
        return execute(() ->
            getAtomicCounter(key).addAndGet(delta)
        );
    }

    @Override
    public long incrementAndGetWithExpire(String key, Duration duration) {
        return execute(() -> {
            RAtomicLong counter = getAtomicCounter(key);
            long value = counter.incrementAndGet();

            if (value == 1L) {
                counter.expire(duration);
            }

            return value;
        });
    }

    @Override
    public long getCounterValue(String key) {
        return execute(() ->
            getAtomicCounter(key).get()
        );
    }

    @Override
    public void deleteCounter(String key) {
        execute(() -> {
            getAtomicCounter(key).delete();
            return null;
        });
    }
}
