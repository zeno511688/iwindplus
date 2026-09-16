/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sumsub.factory;

import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.sumsub.domain.property.SumSubProperty;
import com.iwindplus.base.sumsub.support.SumSubExecuteHandler;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.function.SingletonSupplier;

/**
 * SumSub策略工厂. 负责管理和路由SumSub策略，支持多配置、动态路由.
 *
 * @author zengdegui
 * @since 2026/9/7
 */
@Slf4j
public class SumSubExecuteHandlerFactory implements SmartInitializingSingleton {

    private final SumSubProperty property;
    private final Supplier<Map<String, SumSubExecuteHandler>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param property         属性配置
     * @param executorHandlers 执行器列表
     */
    public SumSubExecuteHandlerFactory(SumSubProperty property, List<SumSubExecuteHandler> executorHandlers) {
        this.property = property;
        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<String, SumSubExecuteHandler>
                strategyMap = executorHandlers
                .stream()
                .collect(Collectors.toMap(
                    SumSubExecuteHandler::getCode,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));

            log.info("Loaded {} strategies={}",
                SumSubExecuteHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 根据配置编码获取SumSub策略.
     *
     * @param code 配置编码
     * @return SumSub策略
     */
    public SumSubExecuteHandler getHandler(String code) {
        final SumSubExecuteHandler handler = getStrategyMap().get(code);
        if (handler == null) {
            log.error("SumSubExecuteHandler invalid strategy={}", code);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        return handler;
    }

    /**
     * 获取默认SumSub策略.
     *
     * @return 默认SumSub策略
     */
    public SumSubExecuteHandler getDefaultHandler() {
        String defaultCode = this.property.getDefaultCode();
        if (defaultCode == null || defaultCode.isBlank()) {
            log.warn("SumSubExecuteHandler default-code not configured");
            return null;
        }
        return getStrategyMap().values().stream()
            .filter(handler -> handler.getCode().equals(defaultCode) && handler.isHealthy())
            .findFirst()
            .orElseGet(() -> {
                log.error("SumSubExecuteHandler default-code invalid code={}", defaultCode);
                return null;
            });
    }

    /**
     * 根据配置编码获取SumSub配置.
     *
     * @param code 配置编码
     * @return SumSub配置
     */
    public SumSubProperty.SumSubConfig getConfig(String code) {
        return this.property.getConfigs().stream()
            .filter(config -> code.equals(config.getCode()))
            .findFirst()
            .orElse(null);
    }

    @Override
    public void afterSingletonsInstantiated() {
        getStrategyMap();
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<String, SumSubExecuteHandler>
     */
    private Map<String, SumSubExecuteHandler> getStrategyMap() {
        return strategyMapSupplier.get();
    }
}
