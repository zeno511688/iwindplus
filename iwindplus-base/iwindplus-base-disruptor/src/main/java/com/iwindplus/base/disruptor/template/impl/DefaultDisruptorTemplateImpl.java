/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.disruptor.template.impl;

import cn.hutool.core.map.MapUtil;
import com.iwindplus.base.disruptor.domain.dto.DisruptorPublishDTO;
import com.iwindplus.base.disruptor.domain.event.DisruptorEvent;
import com.iwindplus.base.disruptor.domain.property.DisruptorMultiProperty;
import com.iwindplus.base.disruptor.template.DisruptorTemplate;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import io.micrometer.tracing.propagation.Propagator;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * Disruptor模板默认实现.
 *
 * @author zengdegui
 * @since 2026/06/18 08:06
 */
@Slf4j
public record DefaultDisruptorTemplateImpl<T>(
    String name,
    DisruptorMultiProperty property,
    Disruptor<DisruptorEvent<T>> disruptor,
    TraceContextPropagator traceContextPropagator) implements DisruptorTemplate<T>, AutoCloseable {

    private static final Propagator.Setter<Map<String, String>> DISRUPTOR_SETTER = Map::put;

    @Override
    public boolean publish(DisruptorPublishDTO<T> entity) {
        final String handlerName = entity.getHandlerName();
        final T data = entity.getData();
        Assert.hasText(handlerName, "handlerName must not be blank");
        Assert.notNull(data, "data must not be null");

        RingBuffer<DisruptorEvent<T>> ringBuffer = disruptor.getRingBuffer();
        Long sequence = this.getSequence(ringBuffer);
        if (sequence == null) {
            return false;
        }

        boolean success = false;

        try {
            DisruptorEvent<T> event = ringBuffer.get(sequence);
            event.clear();

            final Map<String, String> headers = MapUtil.isEmpty(entity.getHeaders())
                ? new HashMap<>(16) : entity.getHeaders();
            event.setHeaders(headers);
            event.setHandlerName(handlerName);
            event.setPublishTime(System.currentTimeMillis());
            event.setSource(entity.getSource());
            event.setDestination(entity.getDestination());
            event.setData(data);

            traceContextPropagator.inject(
                event.getHeaders(),
                DISRUPTOR_SETTER
            );

            success = true;
            return true;
        } catch (Throwable e) {
            log.error("Disruptor publish error, name={}", name, e);

            throw e;
        } finally {
            // 只有填充成功才发布
            if (success) {
                ringBuffer.publish(sequence);

                log.info("Disruptor send success, name={}, sequence={}", name, sequence);
            }
        }
    }

    @Override
    public void close() throws Exception {
        try {
            disruptor.shutdown();
        } catch (Exception ex) {
            log.warn("shutdown disruptor error, name={}", name, ex);
        }
    }

    private Long getSequence(RingBuffer<DisruptorEvent<T>> ringBuffer) {
        try {
            // 非阻塞申请槽位
            return ringBuffer.tryNext();
        } catch (InsufficientCapacityException e) {
            // RingBuffer 已满
            log.warn(
                "Disruptor ringBuffer is full, name={} remainingCapacity={}",
                name, ringBuffer.remainingCapacity());

            return null;
        }
    }
}
