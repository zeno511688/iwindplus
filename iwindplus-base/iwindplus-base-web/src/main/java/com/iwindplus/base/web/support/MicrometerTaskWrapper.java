/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web.support;

import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;

/**
 * DynamicTP (Runnable) 传播 Micrometer Observation/Tracing 上下文 通过 taskWrapperNames: ["micrometer"].
 *
 * @author zengdegui
 * @since 2026/03/26 21:28
 */
@Slf4j
public class MicrometerTaskWrapper implements TaskWrapper {

    private static final String NAME = "micrometer";

    private static ContextSnapshotFactory contextSnapshotFactory;

    /**
     * 初始化
     */
    public static void initialize(ContextSnapshotFactory contextSnapshotFactory) {
        MicrometerTaskWrapper.contextSnapshotFactory = contextSnapshotFactory;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Runnable wrap(Runnable runnable) {
        ContextSnapshot snapshot = contextSnapshotFactory.captureAll();

        return () -> {
            try (ContextSnapshot.Scope ignored = snapshot.setThreadLocals()) {
                runnable.run();
            } catch (Throwable ex) {
                log.error("DynamicTP runnable error.", ex);
                throw ex;
            }
        };
    }
}
