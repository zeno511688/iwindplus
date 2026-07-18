/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web.support;

import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.observation.ObservationRegistry;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.DtpRegistry;
import org.springframework.scheduling.annotation.AsyncConfigurer;

/**
 * DynamicTP异步线程池配置.
 * <p>
 * 所有 @Async 自动支持 Micrometer Context(MDC / Observation / Trace) 传播.
 *
 * @author zengdegui
 * @since 2023/08/29 22:24
 */
@Slf4j
public record AsyncThreadPoolConfigurer(
    ContextSnapshotFactory contextSnapshotFactory,
    ObservationRegistry observationRegistry) implements AsyncConfigurer {

    private static final String DEFAULT_TASK_EXECUTOR_BEAN_NAME = "asyncTaskExecutor";

    /**
     * 所有 @Async 使用 DynamicTP
     */
    @Override
    public Executor getAsyncExecutor() {
        Executor executor = DtpRegistry.getExecutor(DEFAULT_TASK_EXECUTOR_BEAN_NAME);

        return command -> {
            ContextSnapshot snapshot = contextSnapshotFactory.captureAll();

            executor.execute(() -> {
                try (ContextSnapshot.Scope ignored = snapshot.setThreadLocals()) {
                    command.run();
                } catch (Throwable ex) {
                    log.error("DynamicTP Async execute error.", ex);
                    throw ex;
                }
            });
        };
    }
}