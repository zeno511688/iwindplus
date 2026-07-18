/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.operation.impl;

import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.operation.RedissonBaseOperation;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 抽象redisson基本操作.
 *
 * @author zengdegui
 * @since 2026/04/04 11:33
 */
@Slf4j
public abstract class AbstractRedissonBaseOperationImpl implements RedissonBaseOperation {

    /**
     * 执行redis操作.
     *
     * @param supplier 函数
     * @param <T>      泛型
     * @return T
     */
    protected <T> T execute(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable ex) {
            if (ex instanceof BizException bizEx) {
                throw bizEx;
            }
            log.error("Redis操作异常", ex);
            throw new BizException(BizCodeEnum.EXECUTE_ERROR);
        }
    }

    /**
     * reactive执行包装.
     *
     * @param supplier 函数
     * @param <T>      泛型
     * @return Mono<T>
     */
    protected <T> Mono<T> executeReactive(final Supplier<Mono<T>> supplier) {
        return Mono.defer(supplier)
            .onErrorMap(ex -> {
                if (ex instanceof BizException bizEx) {
                    return bizEx;
                }

                log.error("Reactive redis operation error", ex);
                return new BizException(BizCodeEnum.EXECUTE_ERROR);
            });
    }
}
