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

        final Boolean enabledObservation =
            manager.getProperty().getConsumerEnabledObservation(handler.getCluster());

        if (Boolean.FALSE.equals(enabledObservation)) {
            handler.handleBatch(msgs);
            return;
        }

        RocketReceiverObservationContext context =
            new RocketReceiverObservationContext(
                handler.getCluster(),
                handler.getTopic(),
                handler.getGroup(),
                handler.getTag()
            );

        try (TraceScope ignored =
            traceContextPropagator.extract(
                msgs.get(0),
                ROCKET_GETTER
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