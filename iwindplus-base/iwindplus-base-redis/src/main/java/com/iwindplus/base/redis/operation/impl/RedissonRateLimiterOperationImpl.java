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
import com.iwindplus.base.redis.domain.property.RedisProperty;
import com.iwindplus.base.redis.domain.property.RedisProperty.RateLimiterConfig;
import com.iwindplus.base.redis.operation.RedissonRateLimiterOperation;
import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RRateLimiterReactive;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import reactor.core.publisher.Mono;

/**
 * redis限流操作实现类.
 *
 * @author zengdegui
 * @since 2026/04/04 12:17
 */
@Slf4j
public class RedissonRateLimiterOperationImpl implements RedissonRateLimiterOperation {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisProperty property;

    @Override
    public <T> T execute(String name, RateType rateType, Long rate, Duration rateInterval, SupplierThrowable<T> supplier) {
        final RateLimiterConfig cfg = this.property.getRateLimiter();

        final RRateLimiter limiter = this.redissonClient.getRateLimiter(name);
        // 已存在返回false(不更新配置)
        final boolean initialized = limiter.trySetRate(
            Optional.ofNullable(rateType).orElse(cfg.getRateType()),
            Optional.ofNullable(rate).orElse(cfg.getRate()),
            Optional.ofNullable(rateInterval).orElse(cfg.getRateInterval())
        );
        if (!initialized) {
            log.warn("Redis限流已存在，配置不更新 key={}", name);
        }
        // 平滑限流（防抖）
        final boolean acquired = limiter.tryAcquire(1, Duration.ofMillis(100));
        if (!acquired) {
            throw new BizException(BizCodeEnum.REQUEST_TOO_FAST);
        }

        if (supplier == null) {
            return null;
        }

        try {
            return supplier.get();
        } catch (Throwable ex) {
            if (ex instanceof BizException bizEx) {
                throw bizEx;
            }
            log.error("Redis限流执行异常 key={}", name, ex);
            throw new BizException(BizCodeEnum.EXECUTE_ERROR);
        }
    }

    @Override
    public <T> Mono<T> executeReactive(String name, RateType rateType, Long rate, Duration rateInterval, Supplier<Mono<T>> supplier) {
        final RateLimiterConfig cfg = this.property.getRateLimiter();

        final RRateLimiterReactive limiter = redissonClient.reactive().getRateLimiter(name);

        return limiter.trySetRate(
                Optional.ofNullable(rateType).orElse(cfg.getRateType()),
                Optional.ofNullable(rate).orElse(cfg.getRate()),
                Optional.ofNullable(rateInterval).orElse(cfg.getRateInterval())
            )
            .flatMap(init -> limiter.tryAcquire(1, Duration.ofMillis(100)))
            .flatMap(acquired -> {
                if (!acquired) {
                    return Mono.error(new BizException(BizCodeEnum.REQUEST_TOO_FAST));
                }

                if (supplier == null) {
                    return Mono.empty();
                }
                return Mono.defer(supplier);
            });
    }
}
