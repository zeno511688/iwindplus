/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.operation;

import com.iwindplus.base.domain.support.SupplierThrowable;
import java.time.Duration;
import java.util.function.Supplier;
import reactor.core.publisher.Mono;

/**
 * redis防重复提交操作.
 *
 * @author zengdegui
 * @since 2026/04/04 12:11
 */
public interface RedissonRepeatSubmitOperation {

    /**
     * 防重复提交执行方法（阻塞）
     *
     * @param reqKey   请求键
     * @param ttl      过期时间
     * @param supplier 业务逻辑供应者（必填）
     * @param <T>      返回类型
     * @return T
     */
    <T> T execute(
        String reqKey,
        Duration ttl,
        SupplierThrowable<T> supplier);

    /**
     * 防重复提交执行方法（非阻塞）
     *
     * @param reqKey   请求键
     * @param ttl      过期时间
     * @param supplier 业务逻辑供应者（必填）
     * @param <T>      返回类型
     * @return Mono<T>
     */
    <T> Mono<T> executeReactive(
        String reqKey,
        Duration ttl,
        Supplier<Mono<T>> supplier);
}
