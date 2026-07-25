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
import com.lmax.disruptor.InsufficientCapacityException;
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
    public boolean publish(String source, String destination, T data) {
        RingBuffer<DisruptorEvent<T>> ringBuffer = disruptor.getRingBuffer();
        Long sequence = this.getSequence(ringBuffer);
        if (sequence == null) {
            return false;
        }

        boolean success = false;

        try {
            DisruptorEvent<T> event = ringBuffer.get(sequence);
            event.clear();
            event.setName(name);
            event.setPublishTime(System.currentTimeMillis());
            event.setSource(source);
            event.setDestination(destination);
            event.setData(data);

            traceContextPropagator.inject(
                event.getHeaders(),
                DISRUPTOR_SETTER
            );

            success = true;
            return true;
        } catch (Throwable e) {
            log.error("Disruptor publish error,name={}", name, e);

            throw e;
        } finally {
            // 只有填充成功才发布
            if (success) {
                ringBuffer.publish(sequence);
            }
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

    private Long getSequence(RingBuffer<DisruptorEvent<T>> ringBuffer) {
        try {
            // 非阻塞申请槽位
            return ringBuffer.tryNext();
        } catch (InsufficientCapacityException e) {
            // RingBuffer 已满
            log.warn(
                "Disruptor ringBuffer is full, name={}, remainingCapacity={}",
                name,
                ringBuffer.remainingCapacity());

            return null;
        }
    }
}
