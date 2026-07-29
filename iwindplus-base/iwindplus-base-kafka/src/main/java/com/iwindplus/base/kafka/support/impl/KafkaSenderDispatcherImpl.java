/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support.impl;

import com.iwindplus.base.kafka.core.KafkaClusterManager;
import com.iwindplus.base.kafka.domain.dto.KafkaMessageDTO;
import com.iwindplus.base.kafka.support.KafkaSenderDispatcher;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka发送调度器实现类.
 *
 * @author zengdegui
 * @since 2026/05/08 16:36
 */
@Slf4j
public record KafkaSenderDispatcherImpl(
    KafkaClusterManager manager,
    ObservationExecutor observationExecutor) implements KafkaSenderDispatcher {

    @Override
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

        return executor.execute(template, msg);
    }

    private void validate(String cluster, String topic, Object message) {
        Objects.requireNonNull(cluster, "cluster must not be null");
        Objects.requireNonNull(topic, "topic must not be null");
        Objects.requireNonNull(message, "message must not be null");
    }
}
