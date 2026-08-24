/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.monitor.executor;

import com.iwindplus.base.monitor.support.MonitorTemplate;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;

/**
 * 监控统一调用业务层接口.
 *
 * @author zengdegui
 * @since 2026/08/24
 */
public interface MonitorExecutor {

    /**
     * 指标采集操作.
     *
     * @return MonitorTemplate
     */
    MonitorTemplate monitor();

    /**
     * Observation 生命周期操作.
     *
     * @return ObservationExecutor
     */
    ObservationExecutor observation();

    /**
     * Trace 上下文传播操作.
     *
     * @return TraceContextPropagator
     */
    TraceContextPropagator trace();
}
