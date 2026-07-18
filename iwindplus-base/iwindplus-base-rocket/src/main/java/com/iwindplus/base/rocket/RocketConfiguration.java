/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket;

import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import com.iwindplus.base.rocket.core.RocketClusterManager;
import com.iwindplus.base.rocket.core.RocketTemplateRouter;
import com.iwindplus.base.rocket.domain.property.RocketMultiProperty;
import com.iwindplus.base.rocket.listener.RocketMultiListenerBeanPostProcessor;
import com.iwindplus.base.rocket.listener.RocketMultiListenerRegistrar;
import com.iwindplus.base.rocket.support.RocketReceiverDispatcher;
import com.iwindplus.base.rocket.support.RocketSenderDispatcher;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RocketMq配置.
 *
 * @author zengdegui
 * @since 2020/4/28
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "rocket.multi", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({RocketMultiProperty.class})
public class RocketConfiguration {

    /**
     * 创建 RocketClusterManager.
     *
     * @param property                    property
     * @param observationRegistryProvider observationRegistryProvider
     * @return RocketClusterManager
     */
    @Bean
    public RocketClusterManager rocketClusterManager(
        RocketMultiProperty property,
        ObjectProvider<ObservationRegistry> observationRegistryProvider) {
        RocketClusterManager manager = new RocketClusterManager(property,
            observationRegistryProvider.getIfAvailable());
        log.info("RocketClusterManager={}", manager);
        return manager;
    }

    /**
     * 创建 RocketSenderDispatcher.
     *
     * @param manager                集群管理器
     * @param traceContextPropagator traceContextPropagator
     * @param observationExecutor    observationExecutor
     * @return RocketSenderDispatcher
     */
    @Bean
    public RocketSenderDispatcher rocketSenderDispatcher(
        RocketClusterManager manager,
        TraceContextPropagator traceContextPropagator,
        ObservationExecutor observationExecutor) {
        final RocketSenderDispatcher rocketSenderDispatcher = new RocketSenderDispatcher(manager,
            traceContextPropagator, observationExecutor);
        log.info("RocketSenderDispatcher={}", rocketSenderDispatcher);
        return rocketSenderDispatcher;
    }

    /**
     * 创建 RocketTemplateRouter.
     *
     * @param manager    集群管理器
     * @param dispatcher 发送调度器
     * @return RocketTemplateRouter
     */
    @Bean
    public RocketTemplateRouter rocketTemplateRouter(
        RocketClusterManager manager,
        RocketSenderDispatcher dispatcher) {
        final RocketTemplateRouter templateRouter = new RocketTemplateRouter(manager, dispatcher);
        log.info("RocketTemplateRouter={}", templateRouter);
        return templateRouter;
    }

    /**
     * 创建 RocketReceiverDispatcher.
     *
     * @param manager                集群管理器
     * @param traceContextPropagator traceContextPropagator
     * @param observationExecutor    observationExecutor
     * @return RocketReceiverDispatcher
     */
    @Bean
    public RocketReceiverDispatcher rocketReceiverDispatcher(
        RocketClusterManager manager,
        TraceContextPropagator traceContextPropagator,
        ObservationExecutor observationExecutor) {
        final RocketReceiverDispatcher rocketReceiverDispatcher = new RocketReceiverDispatcher(manager,
            traceContextPropagator, observationExecutor);
        log.info("RocketReceiverDispatcher={}", rocketReceiverDispatcher);
        return rocketReceiverDispatcher;
    }

    /**
     * 创建 RocketMultiListenerBeanPostProcessor.
     *
     * @return RocketMultiListenerBeanPostProcessor
     */
    @Bean
    public RocketMultiListenerBeanPostProcessor rocketMultiListenerBeanPostProcessor() {
        final RocketMultiListenerBeanPostProcessor beanPostProcessor = new RocketMultiListenerBeanPostProcessor();
        log.info("RocketMultiListenerBeanPostProcessor={}", beanPostProcessor);
        return beanPostProcessor;
    }

    /**
     * 创建 RocketMultiListenerRegistrar.
     *
     * @param bpp        bpp
     * @param manager    集群管理器
     * @param dispatcher 接收调度器
     * @return RocketMultiListenerRegistrar
     */
    @Bean
    public RocketMultiListenerRegistrar rocketMultiListenerRegistrar(
        RocketMultiListenerBeanPostProcessor bpp,
        RocketClusterManager manager,
        RocketReceiverDispatcher dispatcher) {
        final RocketMultiListenerRegistrar registrar = new RocketMultiListenerRegistrar(bpp, manager, dispatcher);
        log.info("RocketMultiListenerRegistrar={}", registrar);
        return registrar;
    }
}
