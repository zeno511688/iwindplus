/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.monitor.support;

import com.iwindplus.base.monitor.domain.dto.TraceScope;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * Trace上下文传播器.
 *
 * @author zengdegui
 * @since 2026/07/15 11:47
 */
@Slf4j
public record TraceContextPropagator(Tracer tracer, Propagator propagator, TextMapPropagator textMapPropagator) {

    public static final String TRACE_CONTEXT_KEY = "otel.trace.context";

    /**
     * 注入TraceContext
     *
     * @param carrier carrier
     * @param setter  setter
     * @param <C>     泛型
     */
    public <C> void inject(
        C carrier,
        Propagator.Setter<C> setter) {

        Objects.requireNonNull(carrier);
        Objects.requireNonNull(setter);

        Span span = tracer.currentSpan();
        if (span == null) {
            log.warn("[Trace Inject] currentSpan is null");
            return;
        }

        TraceContext context = span.context();

        propagator.inject(
            context,
            carrier,
            setter
        );
    }

    /**
     * 注入TraceContext（Reactor）
     *
     * @param context context
     * @param carrier carrier
     * @param setter  setter
     * @param <C>     泛型
     */
    public <C> void injectReactor(
        Context context,
        C carrier,
        TextMapSetter<C> setter) {

        textMapPropagator.inject(
            context,
            carrier,
            setter
        );
    }

    /**
     * 提取并打开Scope
     *
     * @param carrier carrier
     * @param getter  getter
     * @param <C>     泛型
     * @return TraceScope
     */
    public <C> TraceScope extract(
        C carrier,
        Propagator.Getter<C> getter) {

        Objects.requireNonNull(carrier);
        Objects.requireNonNull(getter);

        Span span =
            propagator.extract(
                carrier,
                getter
            ).start();
        if (span == null) {
            log.warn(
                "[Trace Extract] span create failed"
            );
            return TraceScope.EMPTY;
        }

        Tracer.SpanInScope scope = tracer.withSpan(span);
        return new TraceScope(span, scope);
    }

    /**
     * 提取Context（Reactor）
     *
     * @param carrier carrier
     * @param getter  getter
     * @param <C>     泛型
     * @return Context
     */
    public <C> Context extractReactor(
        C carrier,
        TextMapGetter<C> getter) {

        Objects.requireNonNull(carrier);
        Objects.requireNonNull(getter);

        return textMapPropagator.extract(
            Context.current(),
            carrier,
            getter
        );
    }
}
