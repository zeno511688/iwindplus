/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support;

import com.iwindplus.base.kafka.core.KafkaClusterManager;
import com.iwindplus.base.kafka.domain.dto.KafkaMessageDTO;
import com.iwindplus.base.kafka.support.observation.ClusterKafkaSenderObservationConvention;
import com.iwindplus.base.kafka.support.observation.KafkaSenderObservationContext;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;

/**
 * Kafka发送调度器.
 *
 * @author zengdegui
 * @since 2026/05/08 16:36
 */
@Slf4j
public record KafkaSenderDispatcher(
    KafkaClusterManager manager,
    ObservationExecutor observationExecutor) {

    private static final ClusterKafkaSenderObservationConvention CONVENTION =
        new ClusterKafkaSenderObservationConvention();

    /**
     * 发送
     *
     * @param cluster  集群名称
     * @param topic    主题名称
     * @param key      key
     * @param headers  头
     * @param message  消息体
     * @param executor 执行器
     * @param <T>      泛型
     * @return T
     */
    public <T> T dispatch(
        String cluster,
        String topic,
        String key,
        Map<String, Object> headers,
        String message,
        KafkaSendExecutor<T> executor) {

        return doDispatch(
            cluster,
            topic,
            key,
            headers,
            message,
            executor,
            manager::getTemplate);
    }

    /**
     * 发送
     *
     * @param cluster  集群名称
     * @param topic    主题名称
     * @param key      key
     * @param headers  头
     * @param message  消息体
     * @param executor 执行器
     * @param <T>      泛型
     * @return T
     */
    public <T> T dispatch(
        String cluster,
        String topic,
        String key,
        Map<String, Object> headers,
        String message,
        ReactiveKafkaSendExecutor<T> executor) {

        return doDispatch(
            cluster,
            topic,
            key,
            headers,
            message,
            executor,
            manager::getReactiveTemplate);
    }

    /**
     * 公共调度逻辑
     */
    private <T, P> T doDispatch(
        String cluster,
        String topic,
        String key,
        Map<String, Object> headers,
        String message,
        ExecutorWrapper<T, P> executor,
        TemplateProvider<P> templateProvider) {

        validate(cluster, topic, message);

        P template = templateProvider.getTemplate(cluster);
        KafkaMessageDTO msg = KafkaMessageDTO.builder()
            .cluster(cluster)
            .topic(topic)
            .key(key)
            .headers(headers)
            .message(message)
            .build();

        Boolean enabledObservation = manager.getProperty().getProducerEnabledObservation(cluster);
        if (Boolean.FALSE.equals(enabledObservation)) {
            return executor.execute(template, msg);
        }

        KafkaSenderObservationContext context =
            new KafkaSenderObservationContext(
                cluster,
                topic,
                key);

        return observationExecutor.execute(
            CONVENTION,
            () -> context,
            () -> executor.execute(template, msg)
        );
    }

    private void validate(String cluster, String topic, Object message) {
        Objects.requireNonNull(cluster, "cluster must not be null");
        Objects.requireNonNull(topic, "topic must not be null");
        Objects.requireNonNull(message, "message must not be null");
    }

    /**
     * 抽象执行器接口
     */
    @FunctionalInterface
    private interface ExecutorWrapper<T, P> {

        /**
         * 执行
         *
         * @param producer 模板
         * @param message  消息
         * @return 结果
         */
        T execute(P producer, KafkaMessageDTO message);
    }

    /**
     * 抽象模板提供者
     */
    @FunctionalInterface
    private interface TemplateProvider<P> {

        /**
         * 获取模板
         *
         * @param cluster 集群名称
         * @return 模板
         */
        P getTemplate(String cluster);
    }

    /**
     * 对外同步 Kafka 执行器
     */
    @FunctionalInterface
    public interface KafkaSendExecutor<T> extends ExecutorWrapper<T, KafkaTemplate<String, Object>> {

    }

    /**
     * 对外 Reactive Kafka 执行器
     */
    @FunctionalInterface
    public interface ReactiveKafkaSendExecutor<T> extends ExecutorWrapper<T, ReactiveKafkaProducerTemplate<String, Object>> {

    }
}
