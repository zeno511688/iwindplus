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
import com.iwindplus.base.redis.domain.property.RedisProperty.RepeatSubmitConfig;
import com.iwindplus.base.redis.operation.RedissonBaseOperation;
import com.iwindplus.base.redis.operation.RedissonRepeatSubmitOperation;
import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * redis防重复提交实现类.
 *
 * @author zengdegui
 * @since 2026/05/12 23:40
 */
@Slf4j
public class RedissonRepeatSubmitOperationImpl implements RedissonRepeatSubmitOperation {

    @Resource
    private RedissonBaseOperation redissonBaseOperation;

    @Resource
    private RedisProperty property;

    @Override
    public <T> T execute(String reqKey, Duration ttl, SupplierThrowable<T> supplier) {
        final RepeatSubmitConfig cfg = this.property.getRepeatSubmit();
        final Duration duration = Optional.ofNullable(ttl).orElse(cfg.getTtl());
        final boolean result = this.redissonBaseOperation.setIfAbsent(reqKey, "1", duration);
        if (Boolean.FALSE.equals(result)) {
            throw new BizException(BizCodeEnum.REPEAT_SUBMIT, new Object[]{duration.toSeconds()});
        }

        try {
            return supplier.get();
        } catch (Throwable ex) {
            if (ex instanceof BizException bizEx) {
                throw bizEx;
            }

            log.error("Redis防重复提交执行业务失败 key={}", reqKey, ex);
            throw new BizException(BizCodeEnum.EXECUTE_ERROR);
        }
    }

    @Override
    public <T> Mono<T> executeReactive(String reqKey, Duration ttl, Supplier<Mono<T>> supplier) {
        final RepeatSubmitConfig cfg = this.property.getRepeatSubmit();
        final Duration duration = Optional.ofNullable(ttl).orElse(cfg.getTtl());
        return this.redissonBaseOperation.setIfAbsentMono(reqKey, "1", duration)
            .flatMap(result -> {
                if (Boolean.FALSE.equals(result)) {
                    return Mono.error(new BizException(BizCodeEnum.REPEAT_SUBMIT, new Object[]{duration.toSeconds()}));
                }
                return Mono.defer(supplier);
            })
            .onErrorResume(ex -> {
                if (ex instanceof BizException bizEx) {
                    return Mono.error(bizEx);
                }

                log.error("Redis防重复提交执行业务失败 key={}", reqKey, ex);
                return Mono.error(new BizException(BizCodeEnum.EXECUTE_ERROR));
            });
    }
}
