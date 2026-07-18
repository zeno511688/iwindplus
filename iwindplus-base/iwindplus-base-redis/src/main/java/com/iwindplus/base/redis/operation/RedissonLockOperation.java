/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.operation;

import com.iwindplus.base.domain.support.SupplierThrowable;
import com.iwindplus.base.redis.domain.enums.RedisLockTypeEnum;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import reactor.core.publisher.Mono;

/**
 * redis分布式锁操作.
 *
 * @author zengdegui
 * @since 2026/04/04 12:29
 */
public interface RedissonLockOperation {

    /**
     * 分布式锁执行方法（阻塞）.
     *
     * @param lockType  锁的类型（可选，默认：LOCK（非公平锁））
     * @param lockKey   锁（key，必填）
     * @param waitTime  等待获取锁的最长时间（可选，默认：1）
     * @param leaseTime 租约时间（可选，默认：-1，表示永不过期）
     * @param timeUnit  时间单位（可选，默认：秒(s)）
     * @param supplier  供应者函数式接口，用于执行业务逻辑（必填）
     * @param <T>       泛型返回值
     * @return T
     */
    <T> T execute(
        RedisLockTypeEnum lockType,
        String lockKey,
        Long waitTime,
        Long leaseTime,
        TimeUnit timeUnit,
        SupplierThrowable<T> supplier);

    /**
     * 响应式分布式锁执行方法（非阻塞）
     *
     * @param lockType  锁的类型（可选，默认：LOCK（非公平锁））
     * @param lockKey   锁（key，必填）
     * @param waitTime  等待获取锁的最长时间（可选，默认：1）
     * @param leaseTime 租约时间（可选，默认：-1，表示永不过期）
     * @param timeUnit  时间单位（可选，默认：秒(s)）
     * @param supplier  供应者函数式接口，用于执行业务逻辑（必填）
     * @param <T>       泛型返回值
     * @return T
     */
    <T> Mono<T> executeReactive(
        RedisLockTypeEnum lockType,
        String lockKey,
        Long waitTime,
        Long leaseTime,
        TimeUnit timeUnit,
        Supplier<Mono<T>> supplier);
}
