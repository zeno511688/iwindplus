/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.monitor.executor.impl;

import com.iwindplus.base.monitor.executor.MonitorExecutor;
import com.iwindplus.base.monitor.support.MonitorTemplate;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;

/**
 * 可观测性统一门面，组合指标、Observation 生命周期和 Trace 上下文传播能力.
 *
 * @param monitorTemplate        指标模板
 * @param observationExecutor    Observation 执行器
 * @param traceContextPropagator Trace 上下文传播器
 * @author zengdegui
 * @since 2026/08/24
 */
public record MonitorExecutorImpl(
    MonitorTemplate monitorTemplate,
    ObservationExecutor observationExecutor,
    TraceContextPropagator traceContextPropagator
) implements MonitorExecutor {

    @Override
    public MonitorTemplate monitor() {
        return this.monitorTemplate;
    }

    @Override
    public ObservationExecutor observation() {
        return this.observationExecutor;
    }

    @Override
    public TraceContextPropagator trace() {
        return this.traceContextPropagator;
    }
}
