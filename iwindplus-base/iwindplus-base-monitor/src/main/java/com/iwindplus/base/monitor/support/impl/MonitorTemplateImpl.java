/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.monitor.support.impl;

import com.iwindplus.base.monitor.support.MonitorTemplate;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

/**
 * Micrometer监控模板实现.
 *
 * @author zengdegui
 * @since 2026/07/25 10:21
 */
public class MonitorTemplateImpl implements MonitorTemplate {

    private final MeterRegistry meterRegistry;

    private final ConcurrentHashMap<String, Timer> timers =
        new ConcurrentHashMap<>(16);

    public MonitorTemplateImpl(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Counter counter(String name, Tags tags) {
        return Counter.builder(name)
            .tags(tags)
            .register(meterRegistry);
    }

    @Override
    public Timer getTimer(String name, Tags tags) {
        return Timer.builder(name)
            .tags(tags)
            .publishPercentiles(
                0.5,
                0.95,
                0.99
            )
            .register(meterRegistry);
    }

    @Override
    public <T> T timer(String name, Tags tags, Supplier<T> supplier) {
        return getTimer(name, tags)
            .record(
                supplier
            );
    }

    @Override
    public <T> Gauge gauge(String name,
        Tags tags,
        T obj,
        ToDoubleFunction<T> valueFunction) {
        return Gauge.builder(
                name,
                obj,
                valueFunction
            )
            .tags(tags)
            .register(meterRegistry);
    }
}
