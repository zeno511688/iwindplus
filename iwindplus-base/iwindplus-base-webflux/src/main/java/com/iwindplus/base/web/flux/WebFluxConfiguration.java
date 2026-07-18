/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web.flux;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwindplus.base.web.flux.domain.property.WebFluxProperty;
import com.iwindplus.base.web.flux.support.CustomWebFluxConfigurer;
import io.micrometer.observation.ObservationRegistry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebHttpHandlerBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WebFlux配置.
 *
 * @author zengdegui
 * @since 2025/10/21 23:44
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(WebFluxProperty.class)
@AutoConfigureBefore(WebFluxAutoConfiguration.class)
@ConditionalOnProperty(prefix = "web.flux", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebFluxConfiguration {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private ObservationRegistry observationRegistry;

    @Resource
    private WebFluxProperty property;

    /**
     * webflux观察配置器.
     *
     * @return WebHttpHandlerBuilderCustomizer
     */
    @Bean
    public WebHttpHandlerBuilderCustomizer webHttpHandlerBuilderCustomizer() {
        return builder -> builder.observationRegistry(observationRegistry);
    }

    /**
     * 创建 CustomWebFluxConfigurer.
     *
     * @return CustomWebFluxConfigurer
     */
    @Bean
    public CustomWebFluxConfigurer customWebFluxConfigurer() {
        final CustomWebFluxConfigurer webFluxConfigurer = new CustomWebFluxConfigurer(objectMapper, property);
        log.info("CustomWebFluxConfigurer={}", webFluxConfigurer);
        return webFluxConfigurer;
    }
}
