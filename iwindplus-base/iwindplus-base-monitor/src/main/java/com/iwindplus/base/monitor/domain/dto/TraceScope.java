/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.monitor.domain.dto;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

/**
 * TraceScope.
 *
 * @author zengdegui
 * @since 2026/07/17 00:23
 */
public record TraceScope(Span span, Tracer.SpanInScope scope) implements AutoCloseable {

    public static final TraceScope EMPTY =
        new TraceScope(
            null,
            null
        );

    @Override
    public void close() {
        try {
            if (scope != null) {
                scope.close();
            }
        } finally {
            if (span != null) {
                span.end();
            }
        }
    }
}