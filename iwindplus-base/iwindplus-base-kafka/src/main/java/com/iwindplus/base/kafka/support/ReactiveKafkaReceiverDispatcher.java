/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support;

import com.iwindplus.base.kafka.core.KafkaClusterManager;
import com.iwindplus.base.kafka.support.observation.ClusterKafkaReceiverObservationConvention;
import com.iwindplus.base.kafka.support.observation.KafkaReceiverObservationContext;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

/**
 * reactive kafka接收调度器.
 *
 * @author zengdegui
 * @since 2026/05/12 14:12
 */
@Slf4j
public record ReactiveKafkaReceiverDispatcher(
    KafkaClusterManager manager,
    TraceContextPropagator traceContextPropagator,
    ObservationExecutor observationExecutor) {

    private static final ClusterKafkaReceiverObservationConvention CONVENTION =
        new ClusterKafkaReceiverObservationConvention();

    public static final TextMapGetter<Headers> REACTIVE_KAFKA_GETTER =
        new TextMapGetter<>() {
            @Override
            public Iterable<String> keys(
                Headers carrier) {
                return StreamSupport
                    .stream(
                        carrier.spliterator(),
                        false
                    ).map(Header::key)
                    .toList();
            }

            @Override
            public String get(
                Headers carrier,
                String key) {
                Header header = carrier.lastHeader(key);
                if (header == null
                    || header.value() == null) {
                    return null;
                }

                return new String(
                    header.value(),
                    StandardCharsets.UTF_8
                );
            }
        };

    /**
     * 分发消息.
     */
    public Mono<Void> dispatch(
        ReactiveKafkaMessageHandler handler,
        List<ReceiverRecord<String, Object>> msgs) {

        if (msgs == null || msgs.isEmpty()) {
            return Mono.empty();
        }

        final Boolean enabledObservation =
            manager.getProperty()
                .getConsumerEnabledObservation(
                    handler.getCluster()
                );

        if (Boolean.FALSE.equals(enabledObservation)) {
            return Mono.defer(() -> handler.handleBatch(msgs));
        }

        KafkaReceiverObservationContext context =
            new KafkaReceiverObservationContext(
                handler.getCluster(),
                handler.getTopics(),
                handler.getGroup()
            );

        Context parent =
            traceContextPropagator.extractReactor(
                msgs.get(0).headers(),
                REACTIVE_KAFKA_GETTER
            );

        return observationExecutor.executeMono(
            CONVENTION,
            () -> context,
            () ->
                handler.handleBatch(msgs)
        ).contextWrite(
            reactorContext ->
                reactorContext.put(
                    TraceContextPropagator.TRACE_CONTEXT_KEY,
                    parent
                )
        );
    }
}