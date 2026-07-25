/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.monitor.support;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

/**
 * Micrometer监控模板.
 *
 * @author zengdegui
 * @since 2026/07/25 10:15
 */
public interface MonitorTemplate {

    /**
     * 创建 Counter 计数指标
     *
     * @param name 指标名称
     * @param tags 指标维度标签
     * @return
     */
    Counter counter(
        String name,
        Tags tags
    );

    /**
     * Counter 指标递增
     *
     * @param name 指标名称
     * @param tags 指标维度标签
     */
    default void increment(
        String name,
        Tags tags) {
        counter(name, tags).increment();
    }

    /**
     * 获取 Timer 计时指标
     *
     * @param name 指标名称
     * @param tags 指标维度标签
     * @return
     */
    Timer getTimer(
        String name,
        Tags tags
    );

    /**
     * 创建 Timer 计时指标
     *
     * @param name     指标名称
     * @param tags     指标维度标签
     * @param supplier 计时逻辑
     * @param <T>      泛型
     * @return T
     */
    <T> T timer(
        String name,
        Tags tags,
        Supplier<T> supplier
    );

    /**
     * Timer 计时指标
     *
     * @param name     指标名称
     * @param tags     指标维度标签
     * @param runnable 计时逻辑
     */
    default void timer(
        String name,
        Tags tags,
        Runnable runnable) {
        timer(name, tags, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * 创建 Gauge 指标
     *
     * @param name          指标名称
     * @param tags          指标维度标签
     * @param obj           指标对象
     * @param valueFunction 指标对象属性获取函数
     * @param <T>           泛型
     * @return
     */
    <T> Gauge gauge(
        String name,
        Tags tags,
        T obj,
        ToDoubleFunction<T> valueFunction
    );
}
