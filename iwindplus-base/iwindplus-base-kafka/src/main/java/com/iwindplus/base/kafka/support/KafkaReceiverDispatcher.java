/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support;

import com.iwindplus.base.kafka.core.KafkaClusterManager;
import com.iwindplus.base.kafka.support.observation.CustomKafkaListenerObservationConvention;
import com.iwindplus.base.monitor.domain.dto.TraceScope;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import io.micrometer.tracing.propagation.Propagator;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.support.micrometer.KafkaRecordReceiverContext;

/**
 * Kafka接收调度器.
 *
 * @author zengdegui
 * @since 2026/05/08 16:36
 */
@Slf4j
public record KafkaReceiverDispatcher(
    KafkaClusterManager manager,
    TraceContextPropagator traceContextPropagator,
    ObservationExecutor observationExecutor) {

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

    /**
     * 分发消息.
     */
    public void dispatch(KafkaMessageHandler handler, List<ConsumerRecord<String, Object>> msgs) {
        if (msgs == null || msgs.isEmpty()) {
            return;
        }

        ConsumerRecord<String,Object> record = msgs.get(0);

        KafkaRecordReceiverContext context =
            new KafkaRecordReceiverContext(
                record,
                handler.getListenerId(),
                handler.getClientId(),
                handler.getGroup(),
                () -> handler.getClusterId()
            );

        try (TraceScope ignored =
            traceContextPropagator.extract(
                msgs.get(0).headers(),
                KAFKA_GETTER
            )) {
            observationExecutor.execute(
                CONVENTION,
                () -> context,
                () -> {
                    handler.handleBatch(msgs);
                    return null;
                });
        }
    }
}