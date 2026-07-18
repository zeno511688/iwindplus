/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.operation;

import com.fasterxml.jackson.databind.JavaType;
import com.iwindplus.base.domain.support.SupplierThrowable;
import com.iwindplus.base.redis.domain.enums.IdempotentResultModeEnum;
import java.time.Duration;
import java.util.function.Supplier;
import reactor.core.publisher.Mono;

/**
 * redis幂等操作.
 *
 * @author zengdegui
 * @since 2026/04/04 12:11
 */
public interface RedissonIdempotentOperation {

    /**
     * 幂等执行方法（阻塞）
     *
     * @param reqKey        幂等请求键（可选）
     * @param bizKey        幂等业务键（必填）
     * @param processingTtl 处理中过期时间（可选，默认：30s）
     * @param successTtl    成功过期时间（可选，默认：600s）
     * @param javaType      响应类型
     * @param resultMode    结果处理模式
     * @param supplier      业务逻辑供应者（必填）
     * @param <T>           返回类型
     * @return T
     */
    <T> T execute(
        String reqKey,
        String bizKey,
        Duration processingTtl,
        Duration successTtl,
        JavaType javaType,
        IdempotentResultModeEnum resultMode,
        SupplierThrowable<T> supplier);

    /**
     * 幂等执行方法（非阻塞）
     *
     * @param reqKey        幂等请求键（可选）
     * @param bizKey        幂等业务键（必填）
     * @param processingTtl 处理中过期时间（可选，默认：30s）
     * @param successTtl    成功过期时间（可选，默认：600s）
     * @param javaType      响应类型
     * @param resultMode    结果处理模式
     * @param supplier      业务逻辑供应者（必填）
     * @param <T>           返回类型
     * @return Mono<T>
     */
    <T> Mono<T> executeReactive(
        String reqKey,
        String bizKey,
        Duration processingTtl,
        Duration successTtl,
        JavaType javaType,
        IdempotentResultModeEnum resultMode,
        Supplier<Mono<T>> supplier);
}
