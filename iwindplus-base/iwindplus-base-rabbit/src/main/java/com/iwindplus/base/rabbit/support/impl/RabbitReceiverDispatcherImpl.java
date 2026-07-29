/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.support.impl;

import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import com.iwindplus.base.rabbit.core.RabbitClusterManager;
import com.iwindplus.base.rabbit.support.RabbitMessageHandler;
import com.iwindplus.base.rabbit.support.RabbitReceiverDispatcher;
import com.iwindplus.base.rabbit.support.observation.ClusterRabbitReceiverObservationConvention;
import com.iwindplus.base.rabbit.support.observation.RabbitReceiverObservationContext;
import io.micrometer.tracing.Tracer.SpanInScope;
import io.micrometer.tracing.propagation.Propagator;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;

/**
 * Rabbit接收调度器实现类.
 *
 * @author zengdegui
 * @since 2026/05/08 16:36
 */
@Slf4j
public record RabbitReceiverDispatcherImpl(
    RabbitClusterManager manager,
    TraceContextPropagator traceContextPropagator,
    ObservationExecutor observationExecutor) implements RabbitReceiverDispatcher {

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
    @Override
    public void dispatch(RabbitMessageHandler handler) {
        List<Message> msgs = handler.getMessages();
        if (msgs == null || msgs.isEmpty()) {
            return;
        }

        runWithTrace(
            msgs.get(0),
            () -> execute(handler)
        );
    }

    private Void execute(RabbitMessageHandler handler) {
        if (!enabledObservation(handler)) {
            handler.execute();
            return null;
        }

        RabbitReceiverObservationContext context =
            new RabbitReceiverObservationContext(
                handler.getCluster(),
                handler.getQueues(),
                handler.getGroup()
            );

        observationExecutor.execute(
            CONVENTION,
            () -> context,
            () -> {
                handler.execute();
                return null;
            }
        );

        return null;
    }

    private <T> T runWithTrace(
        Message message,
        Supplier<T> supplier) {

        try (SpanInScope ignored =
            traceContextPropagator
                .extract(message, RABBIT_GETTER)) {

            return supplier.get();
        }
    }

    private boolean enabledObservation(
        RabbitMessageHandler handler) {
        return Boolean.TRUE.equals(
            manager.getProperty()
                .getConsumerEnabledObservation(
                    handler.getCluster()
                )
        );
    }
}