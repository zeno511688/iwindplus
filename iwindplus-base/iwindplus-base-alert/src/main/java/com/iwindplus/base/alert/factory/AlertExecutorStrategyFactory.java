/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.alert.factory;

import com.iwindplus.base.alert.domain.enums.AlertChannelTypeEnum;
import com.iwindplus.base.alert.domain.property.AlertProperty;
import com.iwindplus.base.alert.executor.AlertExecutor;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.function.SingletonSupplier;

/**
 * 告警执行器工厂.
 *
 * @author zengdegui
 * @since 2026/03/03 17:47
 */
@Slf4j
public class AlertExecutorStrategyFactory {

    private final AlertProperty property;
    private final Supplier<Map<AlertChannelTypeEnum, AlertExecutor>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param property         property
     * @param executorProvider 执行器提供者
     */
    public AlertExecutorStrategyFactory(
        AlertProperty property,
        ObjectProvider<AlertExecutor> executorProvider) {

        this.property = property;

        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<AlertChannelTypeEnum, AlertExecutor>
                strategyMap = executorProvider
                .orderedStream()
                .collect(Collectors.toMap(
                    AlertExecutor::getChannelType,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));

            log.info("Loaded {} strategies={}",
                AlertExecutor.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 获取默认执行管理器（FEI_SHU）.
     *
     * @return AlertExecutor
     */
    public AlertExecutor getDefaultAlertExecutor() {
        return getAlertExecutor(property.getDefaultAlertChannel());
    }

    /**
     * 获取执行管理器.
     *
     * @param alertChannelType 告警渠道类型
     * @return AlertExecutor
     */
    public AlertExecutor getAlertExecutor(AlertChannelTypeEnum alertChannelType) {
        AlertExecutor strategy = getStrategyMap().get(alertChannelType);
        if (strategy == null) {
            log.error("AlertExecutor Invalid strategy={}", alertChannelType);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }

        return strategy;
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<AlertChannelTypeEnum, AlertExecutor>
     */
    private Map<AlertChannelTypeEnum, AlertExecutor> getStrategyMap() {
        return strategyMapSupplier.get();
    }
}
