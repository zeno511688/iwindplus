/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.gateway.server.config;

import com.iwindplus.gateway.server.domain.property.GatewayProperty;
import com.iwindplus.gateway.server.util.GatewayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * 网关配置.
 *
 * @author zengdegui
 * @since 2020/4/21
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({GatewayProperty.class})
public class GatewayConfiguration {

    /**
     * 用户IP限流.
     *
     * @return KeyResolver
     */
    @Bean
    @Primary
    public KeyResolver hostAddressKeyResolver() {
        return exchange -> Mono.just(GatewayUtil.getRealIp(exchange));
    }

    /**
     * API路径限流.
     *
     * @return KeyResolver
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getPath().value());
    }
}
