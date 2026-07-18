/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.disruptor.support;

import com.iwindplus.base.disruptor.domain.event.DisruptorEvent;
import com.iwindplus.base.disruptor.factory.DisruptorEventHandlerStrategyFactory;
import com.iwindplus.base.disruptor.support.observation.DisruptorObservationContext;
import com.iwindplus.base.disruptor.support.observation.DisruptorObservationConvention;
import com.iwindplus.base.monitor.domain.dto.TraceScope;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import com.lmax.disruptor.EventHandler;
import io.micrometer.tracing.propagation.Propagator;
import java.util.Map;

/**
 * Disruptor 事件处理器助手.
 *
 * @author zengdegui
 * @since 2026/06/17 20:04
 */
public record DisruptorDispatcherHandler<T>(
    DisruptorEventHandlerStrategyFactory factory,
    TraceContextPropagator traceContextPropagator,
    ObservationExecutor observationExecutor)
    implements EventHandler<DisruptorEvent<T>> {

    private static final DisruptorObservationConvention CONVENTION =
        new DisruptorObservationConvention();

    public static final Propagator.Getter<Map<String, String>> DISRUPTOR_GETTER = Map::get;

    @Override
    public void onEvent(DisruptorEvent<T> event,
        long sequence,
        boolean endOfBatch) {

        DisruptorObservationContext context =
            new DisruptorObservationContext(
                event.getName(),
                String.valueOf(sequence),
                event.getSource(),
                event.getDestination());

        try (TraceScope ignored =
            traceContextPropagator.extract(
                event.getHeaders(),
                DISRUPTOR_GETTER
            )) {
            observationExecutor.execute(
                CONVENTION,
                () -> context,
                () -> {
                    DisruptorEventHandler handler =
                        factory.getDisruptorEventHandler(
                            event.getName());

                    handler.execute(
                        event.getData(),
                        sequence,
                        endOfBatch);
                    return null;
                });
        }
    }
}
