/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.disruptor;

import com.iwindplus.base.disruptor.core.DisruptorManager;
import com.iwindplus.base.disruptor.core.impl.DisruptorManagerImpl;
import com.iwindplus.base.disruptor.domain.property.DisruptorMultiProperty;
import com.iwindplus.base.disruptor.factory.DisruptorEventHandlerStrategyFactory;
import com.iwindplus.base.disruptor.support.DisruptorEventHandler;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Disruptor配置.
 *
 * @author zengdegui
 * @since 2023/08/31 20:32
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "disruptor.multi", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(DisruptorMultiProperty.class)
public class DisruptorConfiguration {

    /**
     * 创建 DisruptorEventHandlerStrategyFactory.
     *
     * @param executorProvider 执行器提供者
     * @return DisruptorEventHandlerStrategyFactory
     */
    @Bean
    public DisruptorEventHandlerStrategyFactory disruptorEventHandlerStrategyFactory(
        ObjectProvider<DisruptorEventHandler<?>> executorProvider) {
        DisruptorEventHandlerStrategyFactory disruptorEventHandlerStrategyFactory =
            new DisruptorEventHandlerStrategyFactory(executorProvider);
        log.info("DisruptorEventHandlerStrategyFactory={}", disruptorEventHandlerStrategyFactory);
        return disruptorEventHandlerStrategyFactory;
    }

    /**
     * 创建 DisruptorManager.
     *
     * @param property               property
     * @param factory                factory
     * @param traceContextPropagator traceContextPropagator
     * @param observationExecutor    observationExecutor,
     * @return DisruptorManager
     */
    @Bean
    public DisruptorManager disruptorManager(
        DisruptorMultiProperty property,
        DisruptorEventHandlerStrategyFactory factory,
        TraceContextPropagator traceContextPropagator,
        ObservationExecutor observationExecutor) {
        DisruptorManager manager = new DisruptorManagerImpl(property, factory, traceContextPropagator, observationExecutor);
        log.info("DisruptorManager={}", manager);
        return manager;
    }
}
