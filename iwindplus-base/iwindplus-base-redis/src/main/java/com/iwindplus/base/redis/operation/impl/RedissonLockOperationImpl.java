/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.operation.impl;

import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.support.SupplierThrowable;
import com.iwindplus.base.redis.domain.enums.RedisLockTypeEnum;
import com.iwindplus.base.redis.domain.property.RedisProperty;
import com.iwindplus.base.redis.domain.property.RedisProperty.LockConfig;
import com.iwindplus.base.redis.operation.RedissonLockOperation;
import jakarta.annotation.Resource;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonClient;
import reactor.core.publisher.Mono;

/**
 * redis分布式锁操作实现类.
 *
 * @author zengdegui
 * @since 2026/04/04 12:17
 */
@Slf4j
public class RedissonLockOperationImpl implements RedissonLockOperation {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisProperty property;

    @Override
    public <T> T execute(RedisLockTypeEnum lockType, String lockKey, Long waitTime, Long leaseTime, TimeUnit timeUnit,
        SupplierThrowable<T> supplier) {
        final LockConfig cfg = this.property.getLock();

        final RLock lock = lockType.getLock(redissonClient, lockKey);

        try {
            final boolean locked = lock.tryLock(
                Optional.ofNullable(waitTime).orElse(cfg.getWaitTime()),
                Optional.ofNullable(leaseTime).orElse(cfg.getLeaseTime()),
                Optional.ofNullable(timeUnit).orElse(TimeUnit.SECONDS)
            );
            if (Boolean.FALSE.equals(locked)) {
                throw new BizException(BizCodeEnum.GET_LOCK_ERROR, new Object[]{lockKey});
            }
            return supplier.get();
        } catch (Throwable ex) {
            if (ex instanceof BizException bizEx) {
                throw bizEx;
            }
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            log.error("Redis分布式锁执行业务失败 key={}", lockKey, ex);
            throw new BizException(BizCodeEnum.EXECUTE_ERROR);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public <T> Mono<T> executeReactive(RedisLockTypeEnum lockType, String lockKey, Long waitTime, Long leaseTime, TimeUnit timeUnit,
        Supplier<Mono<T>> supplier) {
        final LockConfig cfg = this.property.getLock();

        final RLockReactive lock = lockType.getLockReactive(redissonClient, lockKey);

        return lock.tryLock(
            Optional.ofNullable(waitTime).orElse(cfg.getWaitTime()),
            Optional.ofNullable(leaseTime).orElse(cfg.getLeaseTime()),
            Optional.ofNullable(timeUnit).orElse(TimeUnit.SECONDS)
        ).flatMap(locked -> {
            if (Boolean.FALSE.equals(locked)) {
                return Mono.error(new BizException(BizCodeEnum.GET_LOCK_ERROR, new Object[]{lockKey}));
            }

            return Mono.usingWhen(
                Mono.just(lock),
                l -> Mono.defer(supplier),
                RedissonLockOperationImpl::safeUnlock,
                (l, ex) -> safeUnlock(l),
                RedissonLockOperationImpl::safeUnlock
            );
        });
    }

    /**
     * 安全解锁
     *
     * @param lock 锁
     * @return Mono<Void>
     */
    public static Mono<Void> safeUnlock(RLockReactive lock) {
        return lock.unlock().onErrorResume(ex -> {
            if (ex instanceof IllegalMonitorStateException) {
                return Mono.empty();
            }
            return Mono.error(ex);
        });
    }
}
