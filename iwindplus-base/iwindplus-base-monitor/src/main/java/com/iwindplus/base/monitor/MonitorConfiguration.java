/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.monitor;

import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.domain.constant.CommonConstant.ObservationConstant;
import com.iwindplus.base.monitor.domain.property.MonitorProperty;
import com.iwindplus.base.monitor.support.ObservationContextPathResolver;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import io.opentelemetry.context.propagation.TextMapPropagator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationRegistryCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 监控配置.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({MonitorProperty.class})
public class MonitorConfiguration {

    @Resource
    private ObservationRegistry observationRegistry;

    @Resource
    private Tracer tracer;

    @Resource
    private Propagator propagator;

    @Resource
    private TextMapPropagator textMapPropagator;

    /**
     * Observation上下文传播器.
     *
     * @return ObservationExecutor
     */
    @Bean
    public ObservationExecutor observationExecutor() {
        final ObservationExecutor observationExecutor = new ObservationExecutor(observationRegistry);
        log.info("ObservationExecutor={}", observationExecutor);
        return observationExecutor;
    }

    /**
     * Trace上下文传播器.
     *
     * @return TraceContextPropagator
     */
    @Bean
    public TraceContextPropagator traceContextPropagator() {
        final TraceContextPropagator traceContextPropagator = new TraceContextPropagator(tracer, propagator, textMapPropagator);
        log.info("TraceContextPropagator={}", traceContextPropagator);
        return traceContextPropagator;
    }

    /**
     * 创建 MeterRegistryCustomizer<MeterRegistry> （公共标签）.
     *
     * @return MeterRegistryCustomizer<MeterRegistry>
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> meterConfigurer() {
        final String applicationName = SpringUtil.getApplicationName();
        MeterRegistryCustomizer<MeterRegistry> meterRegistryCustomizer =
            (registry) -> registry.config().commonTags(ObservationConstant.APPLICATION, applicationName);
        log.info("MeterRegistryCustomizer<MeterRegistry>={}", meterRegistryCustomizer);
        return meterRegistryCustomizer;
    }

    /**
     * 创建 ObservationRegistryCustomizer<ObservationRegistry>.
     *
     * @param monitorProperty 配置
     * @return ObservationRegistryCustomizer<ObservationRegistry>
     */
    @Bean
    public ObservationRegistryCustomizer<ObservationRegistry> observationConfigurer(MonitorProperty monitorProperty) {
        ObservationRegistryCustomizer<ObservationRegistry> observationRegistryCustomizer = new ObservationContextPathResolver()
            .customizer(monitorProperty);
        log.info("ObservationRegistryCustomizer<ObservationRegistry>={}", observationRegistryCustomizer);
        return observationRegistryCustomizer;
    }
}
