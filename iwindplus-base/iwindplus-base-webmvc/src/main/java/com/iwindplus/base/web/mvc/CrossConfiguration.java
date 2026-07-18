/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web.mvc;

import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.web.domain.property.CrossProperty;
import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域相关配置.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(CrossProperty.class)
@ConditionalOnProperty(prefix = "cross", name = "enabled", havingValue = "true")
public class CrossConfiguration {

    @Resource
    private CrossProperty property;

    /**
     * 创建web端跨域配置.
     *
     * @return CorsFilter
     */

    @Bean
    public CorsFilter corsFilter() {
        final CorsConfiguration corsConfiguration = this.getCorsConfiguration();
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration(CommonConstant.SymbolConstant.BASE_PATH, corsConfiguration);
        final CorsFilter corsFilter = new CorsFilter(urlBasedCorsConfigurationSource);
        log.info("CorsFilter={}", corsFilter);
        return corsFilter;
    }

    private CorsConfiguration getCorsConfiguration() {
        final CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 是否发送cookie信息
        corsConfiguration.setAllowCredentials(Optional.ofNullable(this.property).map(CrossProperty::getAllowCredentials).orElse(Boolean.TRUE));
        // 有效时长
        corsConfiguration.setMaxAge(Optional.ofNullable(this.property).map(CrossProperty::getMaxAge).orElse(null));
        // 允许访问的客户端域名
        corsConfiguration.setAllowedOriginPatterns(Optional.ofNullable(this.property).map(CrossProperty::getAllowedOrigins)
            .orElse(Collections.singletonList(CommonConstant.SymbolConstant.ASTERISK)));
        // 允许服务端访问的客户端请求头
        corsConfiguration.setAllowedHeaders(Optional.ofNullable(this.property).map(CrossProperty::getAllowedHeaders)
            .orElse(Collections.singletonList(CommonConstant.SymbolConstant.ASTERISK)));
        // 允许访问的方法名，GET POST等
        corsConfiguration.setAllowedMethods(Optional.ofNullable(this.property).map(CrossProperty::getAllowedMethods)
            .orElse(Collections.singletonList(CommonConstant.SymbolConstant.ASTERISK)));
        return corsConfiguration;
    }
}
