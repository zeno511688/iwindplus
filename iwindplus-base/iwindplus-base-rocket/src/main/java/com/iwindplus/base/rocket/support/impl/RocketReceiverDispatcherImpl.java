/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.support.impl;

import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import com.iwindplus.base.rocket.core.RocketClusterManager;
import com.iwindplus.base.rocket.support.RocketMessageHandler;
import com.iwindplus.base.rocket.support.RocketReceiverDispatcher;
import com.iwindplus.base.rocket.support.observation.ClusterRocketReceiverObservationConvention;
import com.iwindplus.base.rocket.support.observation.RocketReceiverObservationContext;
import io.micrometer.tracing.Tracer.SpanInScope;
import io.micrometer.tracing.propagation.Propagator;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * Rocket接收调度器实现类.
 *
 * @author zengdegui
 * @since 2026/05/08 16:36
 */
@Slf4j
public record RocketReceiverDispatcherImpl(
    RocketClusterManager manager,
    TraceContextPropagator traceContextPropagator,
    ObservationExecutor observationExecutor) implements RocketReceiverDispatcher {

    private static final ClusterRocketReceiverObservationConvention CONVENTION =
        new ClusterRocketReceiverObservationConvention();

    public static final Propagator.Getter<MessageExt> ROCKET_GETTER =
        (message, key) -> message.getUserProperty(key);

    /**
     * 分发消息.
     */
    @Override
    public void dispatch(RocketMessageHandler handler) {
        List<MessageExt> msgs = handler.getMessages();
        if (msgs == null || msgs.isEmpty()) {
            return;
        }

        runWithTrace(
            msgs.get(0),
            () -> execute(handler)
        );
    }

    private Void execute(RocketMessageHandler handler) {
        if (!enabledObservation(handler)) {
            handler.execute();
            return null;
        }

        RocketReceiverObservationContext context =
            new RocketReceiverObservationContext(
                handler.getCluster(),
                handler.getTopic(),
                handler.getGroup(),
                handler.getTag()
            );

        try {
            observationExecutor.execute(
                CONVENTION,
                () -> context,
                () -> {
                    handler.execute();
                    return null;
                }
            );
        } catch (Throwable e) {
            log.error("RocketReceiverObservationContext error", e);
        }

        return null;
    }

    private <T> T runWithTrace(
        MessageExt message,
        Supplier<T> supplier) {

        try (SpanInScope ignored =
            traceContextPropagator
                .extract(message, ROCKET_GETTER)) {

            return supplier.get();
        }
    }

    private boolean enabledObservation(
        RocketMessageHandler handler) {
        return Boolean.TRUE.equals(
            manager.getProperty()
                .getConsumerEnabledObservation(
                    handler.getCluster()
                )
        );
    }
}