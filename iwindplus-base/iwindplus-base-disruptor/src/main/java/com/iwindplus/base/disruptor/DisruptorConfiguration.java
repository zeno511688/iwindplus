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
import com.iwindplus.base.disruptor.factory.DisruptorEventHandlerFactory;
import com.iwindplus.base.disruptor.support.DisruptorEventHandler;
import com.iwindplus.base.monitor.support.MonitorTemplate;
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
     * 创建 DisruptorEventHandlerFactory.
     *
     * @param executorProvider 执行器提供者
     * @return DisruptorEventHandlerFactory
     */
    @Bean
    public DisruptorEventHandlerFactory disruptorEventHandlerFactory(
        ObjectProvider<DisruptorEventHandler<?>> executorProvider) {
        DisruptorEventHandlerFactory disruptorEventHandlerFactory =
            new DisruptorEventHandlerFactory(executorProvider);
        log.info("DisruptorEventHandlerFactory={}", disruptorEventHandlerFactory);
        return disruptorEventHandlerFactory;
    }

    /**
     * 创建 DisruptorManager.
     *
     * @param property               property
     * @param factory                factory
     * @param traceContextPropagator traceContextPropagator
     * @param observationExecutor    observationExecutor
     * @param monitorTemplate        monitorTemplate
     * @return DisruptorManager
     */
    @Bean
    public DisruptorManager disruptorManager(
        DisruptorMultiProperty property,
        DisruptorEventHandlerFactory factory,
        TraceContextPropagator traceContextPropagator,
        ObservationExecutor observationExecutor,
        MonitorTemplate monitorTemplate) {
        DisruptorManager manager = new DisruptorManagerImpl(property,
            factory, traceContextPropagator, observationExecutor, monitorTemplate);
        log.info("DisruptorManager={}", manager);
        return manager;
    }
}
