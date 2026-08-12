/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.monitor.support;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.ObservationRegistry;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Observation上下文传播器.
 *
 * @author zengdegui
 * @since 2026/07/15
 */
@Slf4j
public record ObservationExecutor(ObservationRegistry observationRegistry) {

    /**
     * 同步执行
     *
     * @param name     名称
     * @param function 函数
     * @param <T>      泛型
     * @return T
     */
    public <T> T execute(
        String name,
        ObservationFunction<T> function) {

        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(function, "function must not be null");

        final Observation observation =
            Observation.createNotStarted(
                name,
                observationRegistry
            );

        return this.doExecute(function, observation);
    }

    /**
     * 同步执行
     *
     * @param name     名称
     * @param supplier 业务逻辑
     * @param <T>      泛型
     * @return T
     */
    public <T> T execute(
        String name,
        Supplier<T> supplier) {

        return this.execute(
            name,
            observation -> supplier.get()
        );
    }

    /**
     * 同步执行
     *
     * @param convention      ObservationConvention
     * @param contextSupplier Context
     * @param supplier        业务逻辑
     * @param <T>             泛型
     * @param <C>             泛型
     * @return T
     */
    public <T, C extends Observation.Context> T execute(
        ObservationConvention<C> convention,
        Supplier<C> contextSupplier,
        Supplier<T> supplier) {

        return this.execute(
            convention,
            contextSupplier,
            observation -> supplier.get()
        );
    }

    /**
     * 同步执行
     *
     * @param convention      ObservationConvention
     * @param contextSupplier Context
     * @param function        函数
     * @param <T>             泛型
     * @param <C>             泛型
     * @return T
     */
    public <T, C extends Observation.Context> T execute(
        ObservationConvention<C> convention,
        Supplier<C> contextSupplier,
        ObservationFunction<T> function) {

        Objects.requireNonNull(convention, "convention must not be null");
        Objects.requireNonNull(contextSupplier, "contextSupplier must not be null");
        Objects.requireNonNull(function, "function must not be null");

        Observation observation =
            Observation.createNotStarted(
                convention,
                contextSupplier,
                observationRegistry
            );

        return this.doExecute(function, observation);
    }

    /**
     * 异步执行
     *
     * @param convention      ObservationConvention
     * @param contextSupplier Context
     * @param supplier        业务逻辑
     * @param <T>             泛型
     * @param <C>             泛型
     * @return CompletionStage<T>
     */
    public <T, C extends Observation.Context> CompletionStage<T> executeAsync(
        ObservationConvention<C> convention,
        Supplier<C> contextSupplier,
        Supplier<CompletionStage<T>> supplier) {

        Objects.requireNonNull(convention, "convention must not be null");
        Objects.requireNonNull(contextSupplier, "contextSupplier must not be null");
        Objects.requireNonNull(supplier, "supplier must not be null");

        Observation observation =
            Observation.createNotStarted(
                convention,
                contextSupplier,
                observationRegistry
            );

        observation.start();

        try {
            final CompletionStage<T> stage =
                Objects.requireNonNull(
                    supplier.get(),
                    "supplier must not return null"
                );

            return stage.whenComplete((result, throwable) -> {
                try (Observation.Scope ignored =
                    observation.openScope()) {

                    if (throwable != null) {
                        observation.error(throwable);
                    }

                } finally {
                    observation.stop();
                }
            });
        } catch (Throwable e) {
            observation.error(e);
            observation.stop();

            throw e;
        }
    }

    /**
     * 执行Observation
     *
     * @param convention      ObservationConvention
     * @param contextSupplier Context
     * @param supplier        业务逻辑
     * @param <T>             泛型
     * @param <C>             泛型
     * @return Mono<T>
     */
    public <T, C extends Observation.Context> Mono<T> executeMono(
        ObservationConvention<C> convention,
        Supplier<C> contextSupplier,
        Supplier<Mono<T>> supplier) {

        Objects.requireNonNull(convention, "convention must not be null");
        Objects.requireNonNull(contextSupplier, "contextSupplier must not be null");
        Objects.requireNonNull(supplier, "supplier must not be null");

        return Mono.defer(() -> {
            Observation observation =
                Observation.createNotStarted(
                    convention,
                    contextSupplier,
                    observationRegistry
                );

            return Mono.using(
                observation::start,
                obs -> Mono.defer(() -> {
                    try (Observation.Scope ignored =
                        obs.openScope()) {

                        return Objects.requireNonNull(
                            supplier.get(),
                            "supplier must not return null"
                        );
                    }
                }).doOnError(obs::error),
                Observation::stop
            );
        });
    }

    private <T> T doExecute(ObservationFunction<T> function, Observation observation) {
        observation.start();

        try {
            try (Observation.Scope ignored = observation.openScope()) {
                return function.execute(observation);
            }
        } catch (Throwable e) {
            observation.error(e);

            throw e;
        } finally {
            observation.stop();
        }
    }

    @FunctionalInterface
    public interface ObservationFunction<T> {

        /**
         * 执行Observation
         *
         * @param observation Observation
         * @return T
         */
        T execute(Observation observation);
    }
}