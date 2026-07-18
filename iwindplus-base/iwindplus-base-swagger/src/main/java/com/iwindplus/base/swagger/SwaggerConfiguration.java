/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.swagger;

import com.iwindplus.base.swagger.domain.property.SwaggerProperty;
import com.iwindplus.base.swagger.listener.SwaggerListener;
import com.iwindplus.base.swagger.service.SwaggerService;
import com.iwindplus.base.swagger.service.impl.SwaggerServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger配置.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(SwaggerProperty.class)
public class SwaggerConfiguration {

    /**
     * 创建 SwaggerService.
     *
     * @return SwaggerService
     */
    @Bean
    public SwaggerService swaggerService() {
        final SwaggerService swaggerService = new SwaggerServiceImpl();
        log.info("SwaggerService={}", swaggerService);
        return swaggerService;
    }

    /**
     * 创建 SwaggerListener.
     *
     * @return SwaggerListener
     */
    @Bean
    public SwaggerListener swaggerListener() {
        final SwaggerListener swaggerListener = new SwaggerListener();
        log.info("SwaggerListener={}", swaggerListener);
        return swaggerListener;
    }
}
