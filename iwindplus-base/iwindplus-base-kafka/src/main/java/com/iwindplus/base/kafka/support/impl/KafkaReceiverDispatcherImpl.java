/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support.impl;

import cn.hutool.core.util.StrUtil;
import com.iwindplus.base.disruptor.core.DisruptorManager;
import com.iwindplus.base.disruptor.domain.dto.DisruptorPublishDTO;
import com.iwindplus.base.disruptor.template.DisruptorTemplate;
import com.iwindplus.base.kafka.core.KafkaClusterManager;
import com.iwindplus.base.kafka.domain.event.KafkaDisruptorEvent;
import com.iwindplus.base.kafka.handler.KafkaDisruptorEventHandler;
import com.iwindplus.base.kafka.support.KafkaMessageHandler;
import com.iwindplus.base.kafka.support.KafkaReceiverDispatcher;
import com.iwindplus.base.kafka.support.observation.CustomKafkaListenerObservationConvention;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import io.micrometer.tracing.Tracer.SpanInScope;
import io.micrometer.tracing.propagation.Propagator;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.support.micrometer.KafkaRecordReceiverContext;

/**
 * Kafka接收调度器实现类.
 *
 * @author zengdegui
 * @since 2026/05/08 16:36
 */
@Slf4j
public record KafkaReceiverDispatcherImpl(
    KafkaClusterManager manager,
    TraceContextPropagator traceContextPropagator,
    ObservationExecutor observationExecutor,
    DisruptorManager<KafkaDisruptorEvent> disruptorManager) implements KafkaReceiverDispatcher {

    private static final CustomKafkaListenerObservationConvention CONVENTION =
        new CustomKafkaListenerObservationConvention();

    public static final Propagator.Getter<Headers> KAFKA_GETTER =
        (headers, key) -> {
            if (headers == null) {
                return null;
            }

            Header header = headers.lastHeader(key);
            if (header == null || header.value() == null) {
                return null;
            }

            return new String(header.value(), StandardCharsets.UTF_8);
        };

    @Override
    public void dispatch(KafkaMessageHandler handler, Consumer<?, ?> consumer) {
        List<ConsumerRecord<String, Object>> msgs = handler.getMessages();
        if (msgs == null || msgs.isEmpty()) {
            return;
        }

        runWithTrace(
            msgs.get(0),
            () -> execute(handler, consumer)
        );
    }

    private Void execute(KafkaMessageHandler handler, Consumer<?, ?> consumer) {
        if (!enabledObservation(handler)) {
            doExecute(handler, consumer);
            return null;
        }

        KafkaRecordReceiverContext context =
            new KafkaRecordReceiverContext(
                handler.getMessages().get(0),
                handler.getListenerId(),
                handler.getClientId(),
                handler.getGroup(),
                handler::getClusterId
            );

        observationExecutor.execute(
            CONVENTION,
            () -> context,
            () -> {
                doExecute(handler, consumer);
                return null;
            }
        );

        return null;
    }

    private void doExecute(KafkaMessageHandler handler, Consumer<?, ?> consumer) {
        final Boolean enableAsyncAcks = manager.getProperty().getConsumerConfig(handler.getCluster()).getEnableAsyncAcks();
        if (Boolean.TRUE.equals(enableAsyncAcks)) {
            final String name = handler.getGroup();
            final String handlerName = StrUtil.lowerFirst(KafkaDisruptorEventHandler.class.getSimpleName());
            final DisruptorTemplate<KafkaDisruptorEvent> template = disruptorManager.getTemplate(name);
            resumeIfNeeded(consumer, template);

            final DisruptorPublishDTO<KafkaDisruptorEvent> entity =
                DisruptorPublishDTO.<KafkaDisruptorEvent>builder()
                    .handlerName(handlerName)
                    .data(handler.getEvent())
                    .source("kafka")
                    .destination("listener")
                    .build();
            if (!template.publish(entity)) {
                consumer.pause(consumer.assignment());
            }
            return;
        }

        handler.execute();
    }

    private void resumeIfNeeded(
        Consumer<?, ?> consumer,
        DisruptorTemplate<KafkaDisruptorEvent> template) {
        Set<TopicPartition> paused = consumer.paused();
        if (!paused.isEmpty() && template.available()) {
            consumer.resume(paused);
            log.info("resume kafka partitions {}", paused);
        }
    }

    private <T> T runWithTrace(
        ConsumerRecord<String, Object> record,
        Supplier<T> supplier) {
        try (SpanInScope ignored =
            traceContextPropagator.extract(record.headers(), KAFKA_GETTER)) {
            return supplier.get();
        }
    }

    private boolean enabledObservation(KafkaMessageHandler handler) {
        return Boolean.TRUE.equals(
            manager.getProperty().getConsumerEnabledObservation(handler.getCluster())
        );
    }
}