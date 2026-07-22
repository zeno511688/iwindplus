/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.support;

import com.iwindplus.base.monitor.domain.dto.TraceScope;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import com.iwindplus.base.rocket.core.RocketClusterManager;
import com.iwindplus.base.rocket.support.observation.ClusterRocketReceiverObservationConvention;
import com.iwindplus.base.rocket.support.observation.RocketReceiverObservationContext;
import io.micrometer.tracing.propagation.Propagator;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * Rocket接收调度器.
 */
@Slf4j
public record RocketReceiverDispatcher(
    RocketClusterManager manager,
    TraceContextPropagator traceContextPropagator,
    ObservationExecutor observationExecutor) {

    private static final ClusterRocketReceiverObservationConvention CONVENTION =
        new ClusterRocketReceiverObservationConvention();

    public static final Propagator.Getter<MessageExt> ROCKET_GETTER =
        (message, key) -> message.getUserProperty(key);

    /**
     * 分发消息.
     */
    public void dispatch(RocketMessageHandler handler, List<MessageExt> msgs) {
        if (msgs == null || msgs.isEmpty()) {
            return;
        }

        runWithTrace(
            msgs.get(0),
            () -> execute(handler, msgs)
        );
    }

    private Void execute(
        RocketMessageHandler handler,
        List<MessageExt> msgs) {

        if (!enabledObservation(handler)) {
            handler.handleBatch(msgs);
            return null;
        }

        RocketReceiverObservationContext context =
            new RocketReceiverObservationContext(
                handler.getCluster(),
                handler.getTopic(),
                handler.getGroup(),
                handler.getTag()
            );

        observationExecutor.execute(
            CONVENTION,
            () -> context,
            () -> {
                handler.handleBatch(msgs);
                return null;
            }
        );

        return null;
    }

    private <T> T runWithTrace(
        MessageExt message,
        Supplier<T> supplier) {

        try (TraceScope ignored =
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