/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web;

import com.iwindplus.base.web.domain.property.FilterProperty;
import com.iwindplus.base.web.domain.property.GlobalErrorProperty;
import com.iwindplus.base.web.domain.property.ResponseBodyProperty;
import com.iwindplus.base.web.support.MicrometerTaskWrapper;
import com.iwindplus.base.web.support.WebManager;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Hooks;

/**
 * Web配置（最终稳定版）
 * <p>
 * ✔ 支持 Gateway / WebFlux ✔ 不丢 traceId ✔ 兼容线程切换
 *
 * @author zengdegui
 * @since 2023/08/31 20:32
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({
    FilterProperty.class,
    GlobalErrorProperty.class,
    ResponseBodyProperty.class
})
public class WebConfiguration {

    @Resource
    private FilterProperty filterProperty;

    @Resource
    private ResponseBodyProperty responseBodyProperty;

    @Resource
    private ContextSnapshotFactory contextSnapshotFactory;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        // 开启 Reactor Context 自动传播（多线程上下文传播不断）
        Hooks.enableAutomaticContextPropagation();
        MicrometerTaskWrapper.initialize(contextSnapshotFactory);
        log.info("多线程上下文传播已启用");
    }

    /**
     * 配置密码解析器，使用BCrypt的方式对密码进行加密和验证
     *
     * @return BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        log.info("PasswordEncoder={}", passwordEncoder);
        return passwordEncoder;
    }

    /**
     * 初始化 ObservationThreadLocalAccessor.
     *
     * @param observationRegistry observationRegistry
     * @return ApplicationRunner
     */
    @Bean
    public ApplicationRunner observationAccessorInitializer(
        ObservationRegistry observationRegistry) {
        return args -> ObservationThreadLocalAccessor.getInstance()
            .setObservationRegistry(observationRegistry);
    }

    /**
     * WebManager
     */
    @Bean
    public WebManager webManager() {
        return new WebManager(
            filterProperty,
            responseBodyProperty
        );
    }
}