/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit;

import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import com.iwindplus.base.rabbit.core.RabbitClusterManager;
import com.iwindplus.base.rabbit.core.RabbitTemplateRouter;
import com.iwindplus.base.rabbit.domain.property.RabbitMultiProperty;
import com.iwindplus.base.rabbit.listener.RabbitMultiListenerBeanPostProcessor;
import com.iwindplus.base.rabbit.listener.RabbitMultiListenerRegistrar;
import com.iwindplus.base.rabbit.support.RabbitReceiverDispatcher;
import com.iwindplus.base.rabbit.support.RabbitSenderDispatcher;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMq配置.
 *
 * @author zengdegui
 * @since 2020/4/28
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "rabbit.multi", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({RabbitMultiProperty.class})
public class RabbitConfiguration {

    /**
     * 创建 RabbitClusterManager.
     *
     * @param property                    property
     * @param observationRegistryProvider observationRegistryProvider
     * @return RabbitClusterManager
     */
    @Bean
    public RabbitClusterManager rabbitClusterManager(
        RabbitMultiProperty property,
        ObjectProvider<ObservationRegistry> observationRegistryProvider) {
        RabbitClusterManager manager = new RabbitClusterManager(
            property, observationRegistryProvider.getIfAvailable());
        log.info("RabbitClusterManager={}", manager);
        return manager;
    }

    /**
     * 创建 RabbitSenderDispatcher.
     *
     * @param manager             集群管理器
     * @param observationExecutor observationExecutor
     * @return RabbitSenderDispatcher
     */
    @Bean
    public RabbitSenderDispatcher rabbitSenderDispatcher(
        RabbitClusterManager manager,
        ObservationExecutor observationExecutor) {
        final RabbitSenderDispatcher rabbitSenderDispatcher = new RabbitSenderDispatcher(manager,
            observationExecutor);
        log.info("RabbitSenderDispatcher={}", rabbitSenderDispatcher);
        return rabbitSenderDispatcher;
    }

    /**
     * 创建 RabbitTemplateRouter.
     *
     * @param manager                集群管理器
     * @param dispatcher             发送调度器
     * @param traceContextPropagator traceContextPropagator
     * @return RabbitTemplateRouter
     */
    @Bean
    public RabbitTemplateRouter rabbitTemplateRouter(
        RabbitClusterManager manager,
        RabbitSenderDispatcher dispatcher,
        TraceContextPropagator traceContextPropagator) {
        final RabbitTemplateRouter templateRouter = new RabbitTemplateRouter(manager, dispatcher, traceContextPropagator);
        log.info("RabbitTemplateRouter={}", templateRouter);
        return templateRouter;
    }

    /**
     * 创建 RabbitReceiverDispatcher.
     *
     * @param manager                集群管理器
     * @param observationExecutor    observationExecutor
     * @param traceContextPropagator traceContextPropagator
     * @return RabbitReceiverDispatcher
     */
    @Bean
    public RabbitReceiverDispatcher rabbitReceiverDispatcher(
        RabbitClusterManager manager,
        TraceContextPropagator traceContextPropagator,
        ObservationExecutor observationExecutor) {
        final RabbitReceiverDispatcher rabbitReceiverDispatcher = new RabbitReceiverDispatcher(manager,
            traceContextPropagator, observationExecutor);
        log.info("RabbitReceiverDispatcher={}", rabbitReceiverDispatcher);
        return rabbitReceiverDispatcher;
    }

    /**
     * 创建 RabbitMultiListenerBeanPostProcessor.
     *
     * @return RabbitMultiListenerBeanPostProcessor
     */
    @Bean
    public RabbitMultiListenerBeanPostProcessor rabbitMultiListenerBeanPostProcessor() {
        final RabbitMultiListenerBeanPostProcessor beanPostProcessor = new RabbitMultiListenerBeanPostProcessor();
        log.info("RabbitMultiListenerBeanPostProcessor={}", beanPostProcessor);
        return beanPostProcessor;
    }

    /**
     * 创建 RabbitMultiListenerRegistrar.
     *
     * @param bpp        bpp
     * @param manager    集群管理器
     * @param dispatcher 接收调度器
     * @return RabbitMultiListenerRegistrar
     */
    @Bean
    public RabbitMultiListenerRegistrar rabbitMultiListenerRegistrar(
        RabbitMultiListenerBeanPostProcessor bpp,
        RabbitClusterManager manager,
        RabbitReceiverDispatcher dispatcher) {
        final RabbitMultiListenerRegistrar registrar = new RabbitMultiListenerRegistrar(bpp, manager, dispatcher);
        log.info("RabbitMultiListenerRegistrar={}", registrar);
        return registrar;
    }
}
