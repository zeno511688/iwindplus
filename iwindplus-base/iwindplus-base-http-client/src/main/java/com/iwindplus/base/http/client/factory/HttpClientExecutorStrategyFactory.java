/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.factory;

import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.domain.property.HttpClientProperty;
import com.iwindplus.base.http.client.executor.HttpClientExecutor;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.function.SingletonSupplier;

/**
 * HTTP客户端策略工厂.
 *
 * @author zengdegui
 * @since 2026/01/21 00:54
 */
@Slf4j
public class HttpClientExecutorStrategyFactory {

    private final HttpClientProperty property;
    private final Supplier<Map<HttpClientTypeEnum, HttpClientExecutor>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param property         property
     * @param executorProvider 执行器提供者
     */
    public HttpClientExecutorStrategyFactory(
        HttpClientProperty property,
        ObjectProvider<HttpClientExecutor> executorProvider) {

        this.property = property;

        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<HttpClientTypeEnum, HttpClientExecutor>
                strategyMap = executorProvider
                .orderedStream()
                .collect(Collectors.toMap(
                    HttpClientExecutor::getClientType,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));

            log.info("Loaded {} strategies={}",
                HttpClientExecutor.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 获取默认执行管理器（REST_CLIENT）.
     *
     * @return HttpClientExecutor
     */
    public HttpClientExecutor getDefaultHttpClientExecutor() {
        return getHttpClientExecutor(property.getDefaultHttpClient());
    }

    /**
     * 获取执行管理器.
     *
     * @param httpClientType 客户端类型
     * @return HttpClientExecutor
     */
    public HttpClientExecutor getHttpClientExecutor(HttpClientTypeEnum httpClientType) {
        HttpClientExecutor strategy = getStrategyMap().get(httpClientType);
        if (strategy == null) {
            log.error("HttpClientExecutor Invalid strategy={}", httpClientType);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }

        return strategy;
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<HttpClientTypeEnum, HttpClientExecutor>
     */
    private Map<HttpClientTypeEnum, HttpClientExecutor> getStrategyMap() {
        return strategyMapSupplier.get();
    }
}
