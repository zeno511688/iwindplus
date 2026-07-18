/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.disruptor.template.impl;

import com.iwindplus.base.disruptor.domain.event.DisruptorEvent;
import com.iwindplus.base.disruptor.template.DisruptorTemplate;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import io.micrometer.tracing.propagation.Propagator;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Disruptor模板默认实现.
 *
 * @author zengdegui
 * @since 2026/06/18 08:06
 */
@Slf4j
public record DefaultDisruptorTemplateImpl<T>(
    String name,
    Disruptor<DisruptorEvent<T>> disruptor,
    TraceContextPropagator traceContextPropagator) implements DisruptorTemplate<T>, AutoCloseable {

    private static final Propagator.Setter<Map<String, String>> DISRUPTOR_SETTER = Map::put;

    @Override
    public void publish(String source, String destination, T data) {
        RingBuffer<DisruptorEvent<T>> ringBuffer = disruptor.getRingBuffer();

        long seq = ringBuffer.next();

        try {
            DisruptorEvent<T> event = ringBuffer.get(seq);
            event.clear();
            event.setName(name);
            event.setSource(source);
            event.setDestination(destination);
            event.setData(data);

            traceContextPropagator.inject(
                event.getHeaders(),
                DISRUPTOR_SETTER
            );
        } catch (Throwable e) {
            log.error("Disruptor publish error,name={}", name, e);

            throw e;
        } finally {
            ringBuffer.publish(seq);
        }
    }

    @Override
    public void close() throws Exception {
        try {
            disruptor.shutdown();
        } catch (Exception e) {
            log.warn(
                "shutdown disruptor error,name={}",
                name,
                e
            );
        }
    }
}
