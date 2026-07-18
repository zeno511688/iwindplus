/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.support;

import com.iwindplus.base.monitor.domain.dto.TraceScope;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import com.iwindplus.base.rabbit.core.RabbitClusterManager;
import com.iwindplus.base.rabbit.support.observation.ClusterRabbitReceiverObservationConvention;
import com.iwindplus.base.rabbit.support.observation.RabbitReceiverObservationContext;
import io.micrometer.tracing.propagation.Propagator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;

/**
 * Rabbit接收调度器.
 *
 * @author zengdegui
 * @since 2026/05/08 16:36
 */
@Slf4j
public record RabbitReceiverDispatcher(
    RabbitClusterManager manager,
    TraceContextPropagator traceContextPropagator,
    ObservationExecutor observationExecutor) {

    private static final ClusterRabbitReceiverObservationConvention CONVENTION =
        new ClusterRabbitReceiverObservationConvention();

    public static final Propagator.Getter<Message> RABBIT_GETTER =
        (message, key) -> {
            Object value =
                message.getMessageProperties()
                    .getHeaders()
                    .get(key);

            return value == null
                ? null
                : value.toString();
        };

    /**
     * 分发消息.
     */
    public void dispatch(RabbitMessageHandler handler, List<Message> msgs) {
        if (msgs == null || msgs.isEmpty()) {
            return;
        }

        final Boolean enabledObservation =
            manager.getProperty().getConsumerEnabledObservation(handler.getCluster());

        if (Boolean.FALSE.equals(enabledObservation)) {
            handler.handleBatch(msgs);
            return;
        }

        RabbitReceiverObservationContext context =
            new RabbitReceiverObservationContext(
                handler.getCluster(),
                handler.getQueues(),
                handler.getGroup()
            );

        try (TraceScope ignored =
            traceContextPropagator.extract(
                msgs.get(0),
                RABBIT_GETTER
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