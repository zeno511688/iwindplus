/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.disruptor.support;

import com.iwindplus.base.disruptor.domain.event.DisruptorEvent;
import com.iwindplus.base.disruptor.domain.property.DisruptorMultiProperty.DisruptorMultiConfig;
import com.iwindplus.base.disruptor.factory.DisruptorEventHandlerStrategyFactory;
import com.iwindplus.base.disruptor.support.observation.DisruptorObservationContext;
import com.iwindplus.base.disruptor.support.observation.DisruptorObservationConvention;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import com.lmax.disruptor.EventHandler;
import io.micrometer.tracing.Tracer.SpanInScope;
import io.micrometer.tracing.propagation.Propagator;
import java.util.Map;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

/**
 * Disruptor 事件处理器助手.
 *
 * @author zengdegui
 * @since 2026/06/17 20:04
 */
@Slf4j
public record DisruptorDispatcherHandler<T>(
    String name,
    DisruptorMultiConfig config,
    DisruptorEventHandlerStrategyFactory factory,
    TraceContextPropagator traceContextPropagator,
    ObservationExecutor observationExecutor)
    implements EventHandler<DisruptorEvent<T>> {

    private static final DisruptorObservationConvention CONVENTION =
        new DisruptorObservationConvention();

    public static final Propagator.Getter<Map<String, String>> DISRUPTOR_GETTER = Map::get;

    @Override
    public void onEvent(
        DisruptorEvent<T> event,
        long sequence,
        boolean endOfBatch) {

        runWithTrace(
            event,
            () -> {
                execute(event, sequence, endOfBatch);
                return null;
            }
        );
    }

    private void execute(DisruptorEvent<T> event, long sequence, boolean endOfBatch) {
        DisruptorEventHandler handler = factory.getDisruptorEventHandler(event.getHandlerName());

        if (Boolean.FALSE.equals(config.getEnabledObservation())) {
            handler.execute(
                event.getData(),
                sequence,
                endOfBatch);
            return;
        }

        DisruptorObservationContext context =
            new DisruptorObservationContext(
                event.getHandlerName(),
                event.getSource(),
                event.getDestination());
        observationExecutor.execute(
            CONVENTION,
            () -> context,
            () -> {
                handler.execute(
                    event.getData(),
                    sequence,
                    endOfBatch);
                return null;
            });

        log.info("Disruptor execute success, name={}, HandlerName={}, const={}",
            name, event.getHandlerName(),
            System.currentTimeMillis() - event.getPublishTime());
    }

    private <T> T runWithTrace(
        DisruptorEvent<T> event,
        Supplier<T> supplier) {

        try (SpanInScope ignored =
            traceContextPropagator
                .extract(event.getHeaders(), DISRUPTOR_GETTER)) {

            return supplier.get();
        }
    }
}
