/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.alert.factory;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.alert.domain.enums.AlertChannelTypeEnum;
import com.iwindplus.base.alert.domain.property.AlertProperty;
import com.iwindplus.base.alert.support.AlertExecuteHandler;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.function.SingletonSupplier;

/**
 * 告警策略工厂. 负责管理和路由告警策略，支持多配置和动态路由.
 *
 * <p>策略结构：{@code Map<AlertChannelTypeEnum, Map<String, AlertExecuteHandler>>}，
 * 第一维为渠道类型，第二维为配置编码，同一渠道可存在多个配置。</p>
 *
 * @author zengdegui
 * @since 2026/03/03 17:47
 */
@Slf4j
public class AlertExecuteHandlerFactory implements SmartInitializingSingleton {

    private final AlertProperty property;
    private final Supplier<Map<AlertChannelTypeEnum, Map<String, AlertExecuteHandler>>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param property         属性配置
     * @param executorHandlers 执行器列表
     */
    public AlertExecuteHandlerFactory(
        AlertProperty property,
        List<AlertExecuteHandler> executorHandlers) {

        this.property = property;

        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<AlertChannelTypeEnum, Map<String, AlertExecuteHandler>>
                strategyMap = executorHandlers
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                    AlertExecuteHandler::getChannelType,
                    LinkedHashMap::new,
                    Collectors.toMap(
                        AlertExecuteHandler::getCode,
                        Function.identity(),
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new
                    )
                ));

            log.info("Loaded {} strategies={}",
                AlertExecuteHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 根据渠道类型获取告警策略（该渠道下优先级最高的可用策略）.
     *
     * @param channelType 渠道类型
     * @return 告警策略
     */
    public AlertExecuteHandler getHandler(AlertChannelTypeEnum channelType) {
        Map<String, AlertExecuteHandler> handlers = getStrategyMap().get(channelType);
        if (handlers == null || handlers.isEmpty()) {
            log.error("AlertExecuteHandler invalid strategy={}", channelType);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        return handlers.values().stream()
            .filter(AlertExecuteHandler::isHealthy)
            .min(Comparator.comparingInt(AlertExecuteHandler::getPriority))
            .orElseThrow(() -> {
                log.error("AlertExecuteHandler no available strategy={}", channelType);
                return new BizException(BizCodeEnum.INVALID_STRATEGY);
            });
    }

    /**
     * 根据渠道类型和配置编码获取告警策略.
     *
     * @param channelType 渠道类型
     * @param code        配置编码
     * @return 告警策略
     */
    public AlertExecuteHandler getHandler(AlertChannelTypeEnum channelType, String code) {
        Map<String, AlertExecuteHandler> handlers = getStrategyMap().get(channelType);
        if (handlers == null) {
            log.error("AlertExecuteHandler invalid strategy={}", channelType);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        AlertExecuteHandler handler = handlers.get(code);
        if (handler == null) {
            log.error("AlertExecuteHandler invalid code={}", code);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        return handler;
    }

    /**
     * 获取默认告警策略.
     *
     * @return 默认告警策略
     */
    public AlertExecuteHandler getDefaultHandler() {
        String defaultCode = this.property.getDefaultCode();
        if (defaultCode == null || defaultCode.isBlank()) {
            log.warn("AlertExecuteHandler default-code not configured");
            return null;
        }
        return getStrategyMap().values().stream()
            .map(handlers -> handlers.get(defaultCode))
            .filter(Objects::nonNull)
            .filter(AlertExecuteHandler::isHealthy)
            .findFirst()
            .orElseGet(() -> {
                log.error("AlertExecuteHandler default-code invalid code={}", defaultCode);
                return null;
            });
    }

    /**
     * 根据渠道类型和配置编码获取告警配置.
     *
     * @param channelType 渠道类型
     * @param code        配置编码
     * @return 告警配置
     */
    public AlertProperty.BaseConfig getConfig(AlertChannelTypeEnum channelType, String code) {
        final List<? extends AlertProperty.BaseConfig> configs = switch (channelType) {
            case FEI_SHU -> this.property.getFeishu();
        };

        if (CollUtil.isEmpty(configs)) {
            return null;
        }

        return configs.stream()
            .filter(config -> code.equals(config.getCode()))
            .findFirst()
            .orElse(null);
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<AlertChannelTypeEnum, Map < String, AlertExecuteHandler>>
     */
    private Map<AlertChannelTypeEnum, Map<String, AlertExecuteHandler>> getStrategyMap() {
        return strategyMapSupplier.get();
    }

    @Override
    public void afterSingletonsInstantiated() {
        getStrategyMap();
    }
}
