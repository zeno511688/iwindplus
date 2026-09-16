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
import com.iwindplus.base.http.client.support.HttpClientExecuteHandler;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.function.SingletonSupplier;

/**
 * HTTP客户端策略工厂.
 *
 * @author zengdegui
 * @since 2026/01/21 00:54
 */
@Slf4j
public class HttpClientExecuteHandlerFactory implements SmartInitializingSingleton {

    private final HttpClientProperty property;
    private final Supplier<Map<HttpClientTypeEnum, HttpClientExecuteHandler>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param property         property
     * @param executorProvider 执行器提供者
     */
    public HttpClientExecuteHandlerFactory(
        HttpClientProperty property,
        ObjectProvider<HttpClientExecuteHandler> executorProvider) {

        this.property = property;

        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<HttpClientTypeEnum, HttpClientExecuteHandler>
                strategyMap = executorProvider
                .orderedStream()
                .collect(Collectors.toMap(
                    HttpClientExecuteHandler::getClientType,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));

            log.info("Loaded {} strategies={}",
                HttpClientExecuteHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 获取默认执行管理器（REST_CLIENT）.
     *
     * @return HttpClientExecuteHandler
     */
    public HttpClientExecuteHandler getDefaultHandler() {
        return getHandler(property.getDefaultHttpClient());
    }

    /**
     * 获取执行管理器.
     *
     * @param httpClientType 客户端类型
     * @return HttpClientExecuteHandler
     */
    public HttpClientExecuteHandler getHandler(HttpClientTypeEnum httpClientType) {
        HttpClientExecuteHandler strategy = getStrategyMap().get(httpClientType);
        if (strategy == null) {
            log.error("HttpClientExecuteHandler Invalid strategy={}", httpClientType);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }

        return strategy;
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<HttpClientTypeEnum, HttpClientExecuteHandler>
     */
    private Map<HttpClientTypeEnum, HttpClientExecuteHandler> getStrategyMap() {
        return strategyMapSupplier.get();
    }

    @Override
    public void afterSingletonsInstantiated() {
        getStrategyMap();
    }
}
