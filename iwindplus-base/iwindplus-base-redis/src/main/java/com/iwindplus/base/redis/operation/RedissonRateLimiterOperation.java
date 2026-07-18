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
import org.redisson.api.RateType;
import reactor.core.publisher.Mono;

/**
 * redis限流操作.
 *
 * @author zengdegui
 * @since 2026/04/04 12:29
 */
public interface RedissonRateLimiterOperation {

    /**
     * 限流执行方法（阻塞）
     *
     * @param name         限流器名称（key）
     * @param rateType     限流类型（可选，默认：OVERALL）
     * @param rate         速率阈值（每秒请求数，可选，默认：1000）
     * @param rateInterval 速率时间间隔（可选，默认：1s）
     * @param supplier     供应者函数式接口，用于执行业务逻辑（可选）
     * @param <T>          泛型返回值
     * @return T
     */
    <T> T execute(
        String name,
        RateType rateType,
        Long rate,
        Duration rateInterval,
        SupplierThrowable<T> supplier);

    /**
     * 响应式限流执行方法（非阻塞）
     *
     * @param name         限流器名称（key）
     * @param rateType     限流类型（可选，默认：OVERALL）
     * @param rate         速率阈值（每秒请求数，可选，默认：1000）
     * @param rateInterval 速率时间间隔（可选，默认：1s）
     * @param supplier     响应式业务逻辑供应者（可选）
     * @param <T>          返回类型
     * @return Mono<T>
     */
    <T> Mono<T> executeReactive(
        String name,
        RateType rateType,
        Long rate,
        Duration rateInterval,
        Supplier<Mono<T>> supplier);
}
