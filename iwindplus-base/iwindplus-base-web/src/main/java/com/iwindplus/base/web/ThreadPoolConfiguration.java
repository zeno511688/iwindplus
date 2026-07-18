/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web;

import com.iwindplus.base.web.support.AsyncThreadPoolConfigurer;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 线程池配置
 *
 * @author zengdegui
 * @since 2023/08/29 22:24
 */
@Slf4j
@Configuration
public class ThreadPoolConfiguration {

    /**
     * Micrometer Context(MDC / Observation / Trace) 创建工厂
     *
     * @return ContextSnapshotFactory
     */
    @Bean
    public ContextSnapshotFactory contextSnapshotFactory() {
        final ContextSnapshotFactory contextSnapshotFactory = ContextSnapshotFactory.builder().build();
        log.info("ContextSnapshotFactory={}", contextSnapshotFactory);
        return contextSnapshotFactory;
    }

    /**
     * 创建AsyncThreadPoolConfigurer
     *
     * @param contextSnapshotFactory contextSnapshotFactory
     * @param observationRegistry    observationRegistry
     * @return AsyncThreadPoolConfigurer
     */
    @Bean
    public AsyncThreadPoolConfigurer asyncThreadPoolConfigurer(
        ContextSnapshotFactory contextSnapshotFactory,
        ObservationRegistry observationRegistry) {
        final AsyncThreadPoolConfigurer asyncThreadPoolConfigurer = new AsyncThreadPoolConfigurer(
            contextSnapshotFactory, observationRegistry);
        log.info("AsyncThreadPoolConfigurer={}", asyncThreadPoolConfigurer);
        return asyncThreadPoolConfigurer;
    }
}