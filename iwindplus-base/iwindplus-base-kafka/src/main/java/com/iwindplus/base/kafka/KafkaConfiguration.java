/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka;

import com.iwindplus.base.kafka.core.KafkaClusterManager;
import com.iwindplus.base.kafka.core.KafkaTemplateRouter;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty;
import com.iwindplus.base.kafka.listener.KafkaMultiListenerBeanPostProcessor;
import com.iwindplus.base.kafka.listener.KafkaMultiListenerRegistrar;
import com.iwindplus.base.kafka.support.KafkaDlqHandler;
import com.iwindplus.base.kafka.support.KafkaMetrics;
import com.iwindplus.base.kafka.support.KafkaReceiverDispatcher;
import com.iwindplus.base.kafka.support.KafkaRetryHandler;
import com.iwindplus.base.kafka.support.KafkaSenderDispatcher;
import com.iwindplus.base.kafka.support.ReactiveKafkaReceiverDispatcher;
import com.iwindplus.base.kafka.support.impl.DefaultKafkaDlqHandler;
import com.iwindplus.base.kafka.support.impl.DefaultKafkaRetryHandler;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
     * @return KafkaClusterManager
     */
    @Bean
    public KafkaClusterManager kafkaClusterManager(
        KafkaMultiProperty property,
        ObjectProvider<ObservationRegistry> observationRegistryProvider) {
        KafkaClusterManager manager = new KafkaClusterManager(
            property,
            observationRegistryProvider.getIfAvailable());
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
        final KafkaSenderDispatcher kafkaSenderDispatcher = new KafkaSenderDispatcher(manager,
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
     * @return KafkaReceiverDispatcher
     */
    @Bean
    public KafkaReceiverDispatcher kafkaReceiverDispatcher(
        KafkaClusterManager manager,
        TraceContextPropagator traceContextPropagator,
        ObservationExecutor observationExecutor) {
        final KafkaReceiverDispatcher kafkaReceiverDispatcher = new KafkaReceiverDispatcher(manager,
            traceContextPropagator, observationExecutor);
        log.info("KafkaReceiverDispatcher={}", kafkaReceiverDispatcher);
        return kafkaReceiverDispatcher;
    }

    /**
     * 创建 RocketReceiverDispatcher.
     *
     * @param manager                集群管理器
     * @param traceContextPropagator traceContextPropagator
     * @param observationExecutor    observationExecutor
     * @return ReactiveKafkaReceiverDispatcher
     */
    @Bean
    public ReactiveKafkaReceiverDispatcher reactiveKafkaReceiverDispatcher(
        KafkaClusterManager manager,
        TraceContextPropagator traceContextPropagator,
        ObservationExecutor observationExecutor) {
        final ReactiveKafkaReceiverDispatcher reactiveKafkaReceiverDispatcher = new ReactiveKafkaReceiverDispatcher(manager,
            traceContextPropagator, observationExecutor);
        log.info("ReactiveKafkaReceiverDispatcher={}", reactiveKafkaReceiverDispatcher);
        return reactiveKafkaReceiverDispatcher;
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
     * 创建 KafkaMetrics.
     *
     * @param meterRegistry meterRegistry
     * @return KafkaMetrics
     */
    @Bean
    public KafkaMetrics kafkaMetrics(MeterRegistry meterRegistry) {
        final KafkaMetrics kafkaMetrics = new KafkaMetrics(meterRegistry);
        log.info("KafkaMetrics={}", kafkaMetrics);
        return kafkaMetrics;
    }

    /**
     * 创建 KafkaMultiListenerRegistrar.
     *
     * @param bpp                bpp
     * @param manager            集群管理器
     * @param dispatcher         接收调度器
     * @param reactiveDispatcher reactive接收调度器
     * @param retryHandler       重试助手
     * @param dlqHandler         死信队列助手
     * @return KafkaMultiListenerRegistrar
     */
    @Bean
    public KafkaMultiListenerRegistrar kafkaMultiListenerRegistrar(
        KafkaMultiListenerBeanPostProcessor bpp,
        KafkaClusterManager manager,
        KafkaReceiverDispatcher dispatcher,
        ReactiveKafkaReceiverDispatcher reactiveDispatcher,
        KafkaRetryHandler retryHandler,
        KafkaDlqHandler dlqHandler,
        KafkaMetrics kafkaMetrics) {
        final KafkaMultiListenerRegistrar registrar = new KafkaMultiListenerRegistrar(
            bpp, manager, dispatcher, reactiveDispatcher, retryHandler, dlqHandler, kafkaMetrics);
        log.info("KafkaMultiListenerRegistrar={}", registrar);
        return registrar;
    }

    /**
     * 创建 KafkaRetryHandler.
     *
     * @param property property
     * @param manager  集群管理器
     * @return KafkaRetryHandler
     */
    @Bean
    public KafkaRetryHandler defaultKafkaRetryHandler(KafkaMultiProperty property, KafkaClusterManager manager) {
        return new DefaultKafkaRetryHandler(property, manager);
    }

    /**
     * 创建 KafkaDlqHandler.
     *
     * @param property property
     * @param manager  集群管理器
     * @return KafkaDlqHandler
     */
    @Bean
    public KafkaDlqHandler defaultKafkaDlqHandler(KafkaMultiProperty property, KafkaClusterManager manager) {
        return new DefaultKafkaDlqHandler(property, manager);
    }
}
