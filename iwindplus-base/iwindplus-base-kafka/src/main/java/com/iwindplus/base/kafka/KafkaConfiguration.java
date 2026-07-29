/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka;

import com.iwindplus.base.disruptor.core.DisruptorManager;
import com.iwindplus.base.kafka.core.KafkaClusterManager;
import com.iwindplus.base.kafka.core.KafkaTemplateRouter;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty;
import com.iwindplus.base.kafka.handler.KafkaDisruptorEventHandler;
import com.iwindplus.base.kafka.listener.KafkaMultiListenerBeanPostProcessor;
import com.iwindplus.base.kafka.listener.KafkaMultiListenerRegistrar;
import com.iwindplus.base.kafka.support.KafkaListenerInvoker;
import com.iwindplus.base.kafka.support.KafkaMessageHandler;
import com.iwindplus.base.kafka.support.KafkaReceiverDispatcher;
import com.iwindplus.base.kafka.support.KafkaSenderDispatcher;
import com.iwindplus.base.kafka.support.impl.KafkaListenerInvokerImpl;
import com.iwindplus.base.kafka.support.impl.KafkaReceiverDispatcherImpl;
import com.iwindplus.base.kafka.support.impl.KafkaSenderDispatcherImpl;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Kafka配置.
 *
 * @author zengdegui
 * @since 2020/4/28
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "kafka.multi", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(KafkaMultiProperty.class)
public class KafkaConfiguration {

    /**
     * 创建 KafkaClusterManager.
     *
     * @param property                    property
     * @param observationRegistryProvider observationRegistryProvider
     * @param applicationContext          applicationContext
     * @return KafkaClusterManager
     */
    @Bean
    public KafkaClusterManager kafkaClusterManager(
        KafkaMultiProperty property,
        ObjectProvider<ObservationRegistry> observationRegistryProvider,
        ApplicationContext applicationContext) {
        KafkaClusterManager manager = new KafkaClusterManager(
            property,
            observationRegistryProvider.getIfAvailable(),
            applicationContext);
        log.info("KafkaClusterManager={}", manager);
        return manager;
    }

    /**
     * 创建 KafkaSenderDispatcher.
     *
     * @param manager             集群管理器
     * @param observationExecutor observationExecutor
     * @return KafkaSenderDispatcher
     */
    @Bean
    public KafkaSenderDispatcher kafkaSenderDispatcher(
        KafkaClusterManager manager,
        ObservationExecutor observationExecutor) {
        final KafkaSenderDispatcher kafkaSenderDispatcher = new KafkaSenderDispatcherImpl(manager,
            observationExecutor);
        log.info("KafkaSenderDispatcher={}", kafkaSenderDispatcher);
        return kafkaSenderDispatcher;
    }

    /**
     * 创建 KafkaTemplateRouter.
     *
     * @param manager                模板管理器
     * @param dispatcher             发送调度器
     * @param traceContextPropagator traceContextPropagator
     * @return KafkaTemplateRouter
     */
    @Bean
    public KafkaTemplateRouter kafkaTemplateRouter(
        KafkaClusterManager manager,
        KafkaSenderDispatcher dispatcher,
        TraceContextPropagator traceContextPropagator) {
        final KafkaTemplateRouter templateRouter = new KafkaTemplateRouter(
            manager, dispatcher, traceContextPropagator);
        log.info("KafkaTemplateRouter={}", templateRouter);
        return templateRouter;
    }

    /**
     * 创建 KafkaReceiverDispatcher.
     *
     * @param manager                集群管理器
     * @param traceContextPropagator traceContextPropagator
     * @param observationExecutor    observationExecutor
     * @param disruptorManager       disruptorManager
     * @return KafkaReceiverDispatcher
     */
    @Bean
    public KafkaReceiverDispatcher kafkaReceiverDispatcher(
        KafkaClusterManager manager,
        TraceContextPropagator traceContextPropagator,
        ObservationExecutor observationExecutor,
        DisruptorManager<KafkaMessageHandler> disruptorManager) {
        final KafkaReceiverDispatcher kafkaReceiverDispatcher = new KafkaReceiverDispatcherImpl(manager,
            traceContextPropagator, observationExecutor, disruptorManager);
        log.info("KafkaReceiverDispatcher={}", kafkaReceiverDispatcher);
        return kafkaReceiverDispatcher;
    }

    /**
     * 创建 KafkaMultiListenerBeanPostProcessor.
     *
     * @return KafkaMultiListenerBeanPostProcessor
     */
    @Bean
    public KafkaMultiListenerBeanPostProcessor kafkaMultiListenerBeanPostProcessor() {
        final KafkaMultiListenerBeanPostProcessor beanPostProcessor = new KafkaMultiListenerBeanPostProcessor();
        log.info("KafkaMultiListenerBeanPostProcessor={}", beanPostProcessor);
        return beanPostProcessor;
    }

    /**
     * 创建 KafkaDisruptorEventHandler.
     *
     * @return KafkaDisruptorEventHandler
     */
    @Bean
    public KafkaDisruptorEventHandler kafkaDisruptorEventHandler() {
        final KafkaDisruptorEventHandler kafkaDisruptorEventHandler = new KafkaDisruptorEventHandler();
        log.info("KafkaDisruptorEventHandler={}", kafkaDisruptorEventHandler);
        return kafkaDisruptorEventHandler;
    }

    /**
     * 创建 KafkaListenerInvoker.
     *
     * @return KafkaListenerInvoker
     */
    @Bean
    public KafkaListenerInvoker kafkaListenerInvoker() {
        final KafkaListenerInvoker kafkaListenerInvoker = new KafkaListenerInvokerImpl();
        log.info("KafkaListenerInvoker={}", kafkaListenerInvoker);
        return kafkaListenerInvoker;
    }

    /**
     * 创建 KafkaMultiListenerRegistrar.
     *
     * @param applicationContext applicationContext
     * @param bpp                bpp
     * @param listenerInvoker    listenerInvoker
     * @param manager            集群管理器
     * @param dispatcher         接收调度器
     * @return KafkaMultiListenerRegistrar
     */
    @Bean
    public KafkaMultiListenerRegistrar kafkaMultiListenerRegistrar(
        ApplicationContext applicationContext,
        KafkaMultiListenerBeanPostProcessor bpp,
        KafkaListenerInvoker listenerInvoker,
        KafkaClusterManager manager,
        KafkaReceiverDispatcher dispatcher) {
        final KafkaMultiListenerRegistrar registrar = new KafkaMultiListenerRegistrar(
            applicationContext, bpp, listenerInvoker, manager, dispatcher);
        log.info("KafkaMultiListenerRegistrar={}", registrar);
        return registrar;
    }
}
