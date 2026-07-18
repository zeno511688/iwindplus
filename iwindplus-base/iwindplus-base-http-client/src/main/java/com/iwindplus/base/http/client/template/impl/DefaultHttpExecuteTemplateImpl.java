/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.template.impl;

import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.http.client.domain.dto.HttpExecuteResultDTO;
import com.iwindplus.base.http.client.domain.property.HttpClientProperty;
import com.iwindplus.base.http.client.support.observation.HttpClientObservationContext;
import com.iwindplus.base.http.client.support.observation.HttpClientObservationConvention;
import com.iwindplus.base.http.client.template.HttpExecuteTemplate;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.PrematureCloseException;

/**
 * HTTP 执行统一模板（同步 / 异步）默认实现.
 *
 * @author zengdegui
 * @since 2026/02/07 13:11
 */
@Slf4j
public record DefaultHttpExecuteTemplateImpl(
    HttpClientProperty property,
    ObservationExecutor observationExecutor,
    CircuitBreakerRegistry circuitBreakerRegistry) implements HttpExecuteTemplate {

    private static final HttpClientObservationConvention CONVENTION =
        new HttpClientObservationConvention();

    @Override
    public HttpExecuteResultDTO execute(
        String client,
        String method,
        String url,
        Supplier<HttpExecuteResultDTO> supplier) {

        HttpClientObservationContext context = newContext(client, method, url);
        if (context == null) {
            return doExecute(client, null, supplier);
        }

        return observationExecutor.execute(
            CONVENTION,
            () -> context,
            () -> doExecute(client, context, supplier)
        );
    }

    @Override
    public CompletionStage<HttpExecuteResultDTO> executeAsync(
        String client,
        String method,
        String url,
        Supplier<CompletionStage<HttpExecuteResultDTO>> supplier) {

        HttpClientObservationContext context = newContext(client, method, url);
        if (context == null) {
            return doExecuteAsync(client, null, supplier);
        }

        return observationExecutor.executeAsync(
            CONVENTION,
            () -> context,
            () -> doExecuteAsync(client, context, supplier));
    }

    private HttpExecuteResultDTO doExecute(
        String client,
        HttpClientObservationContext context,
        Supplier<HttpExecuteResultDTO> supplier) {
        final long start = System.nanoTime();
        try {
            HttpExecuteResultDTO result =
                executeWithCircuitBreaker(client, supplier);

            updateContext(context, result, null);

            if (result != null && result.error() != null) {
                exceptionProcess(client, result.error(), context, start);
            }

            return result;
        } catch (Throwable ex) {
            updateContext(context, null, ex);

            exceptionProcess(client, ex, context, start);

            throw ex;
        }
    }

    private CompletionStage<HttpExecuteResultDTO> doExecuteAsync(
        String client,
        HttpClientObservationContext context,
        Supplier<CompletionStage<HttpExecuteResultDTO>> supplier) {

        final long start = System.nanoTime();

        return executeAsyncWithCircuitBreaker(client, supplier)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    updateContext(context, null, ex);
                    exceptionProcess(client, ex, context, start);
                } else {
                    updateContext(context, result, null);
                    if (result != null && result.error() != null) {
                        exceptionProcess(client, result.error(), context, start);
                    }
                }
            });
    }

    private void updateContext(
        HttpClientObservationContext context,
        HttpExecuteResultDTO result,
        Throwable throwable) {

        if (context == null) {
            return;
        }

        if (result != null) {
            context.setStatus(result.status());

            if (throwable == null && result.error() instanceof Throwable error) {
                throwable = error;
            }
        }

        if (throwable != null) {
            context.setError(throwable);
        }
    }

    private HttpClientObservationContext newContext(
        String client,
        String method,
        String url) {

        if (Boolean.FALSE.equals(property.getEnabledObservationCustom())) {
            return null;
        }

        return new HttpClientObservationContext(client, method, url);
    }

    private HttpExecuteResultDTO executeWithCircuitBreaker(
        String client,
        Supplier<HttpExecuteResultDTO> supplier) {

        CircuitBreaker cb = getCircuitBreaker(client);

        return cb == null
            ? supplier.get()
            : CircuitBreaker.decorateSupplier(cb, supplier).get();
    }

    private CompletionStage<HttpExecuteResultDTO> executeAsyncWithCircuitBreaker(
        String client,
        Supplier<CompletionStage<HttpExecuteResultDTO>> supplier) {

        CircuitBreaker cb = getCircuitBreaker(client);
        try {
            return cb == null
                ? supplier.get()
                : CircuitBreaker.decorateCompletionStage(cb, supplier).get();
        } catch (Throwable ex) {
            CompletableFuture<HttpExecuteResultDTO> future =
                new CompletableFuture<>();

            future.completeExceptionally(ex);

            return future;
        }
    }

    private CircuitBreaker getCircuitBreaker(String client) {
        if (Boolean.TRUE.equals(property.getEnabledCircuitBreaker())
            && circuitBreakerRegistry != null) {

            return circuitBreakerRegistry.circuitBreaker(client);
        }

        return null;
    }

    private void exceptionProcess(
        String client,
        Object error,
        HttpClientObservationContext context,
        long start) {

        final long cost = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        final String method = context != null ? context.getMethod() : "";
        final String url = context != null ? context.getUrl() : "";

        if (error instanceof BizException ex) {
            log.warn(
                "{} {} {} -> (cost={}ms) ERROR={} {}",
                client,
                method,
                url,
                cost,
                ex.getClass().getSimpleName(),
                ex.getMessage());

            return;
        }

        if (error instanceof Throwable ex) {
            // 网络瞬时异常 / 熔断 / timeout
            // 避免大量 error stack
            if (isRetryable(ex)) {
                log.warn(
                    "{} {} {} -> (cost={}ms) ERROR={} {}",
                    client,
                    method,
                    url,
                    cost,
                    ex.getClass().getSimpleName(),
                    ex.getMessage());

                return;
            }

            log.error(
                "{} {} {} -> (cost={}ms) ERROR={}",
                client,
                method,
                url,
                cost,
                ex.getClass().getSimpleName(),
                ex);

            return;
        }

        log.error(
            "{} {} {} -> (cost={}ms) ERROR={}",
            client,
            method,
            url,
            cost,
            error);
    }

    private boolean isRetryable(Throwable ex) {
        return ex instanceof ConnectException || ex instanceof ReadTimeoutException || ex instanceof WriteTimeoutException
            || ex instanceof SocketTimeoutException || ex instanceof TimeoutException || ex instanceof PrematureCloseException
            || ex instanceof CallNotPermittedException || isRetryableIOException(ex);
    }

    private boolean isRetryableIOException(Throwable ex) {
        if (!(ex instanceof IOException ioEx)) {
            return false;
        }

        String msg = Objects.toString(ioEx.getMessage(), "")
            .toLowerCase();

        return msg.contains("connection reset")
            || msg.contains("broken pipe")
            || msg.contains("connection aborted");
    }
}