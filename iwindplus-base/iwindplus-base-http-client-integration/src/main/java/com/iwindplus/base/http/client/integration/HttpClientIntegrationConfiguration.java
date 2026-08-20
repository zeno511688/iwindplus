/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration;

import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.http.client.integration.service.AddressService;
import com.iwindplus.base.http.client.integration.service.impl.AddressServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HTTP客户端集成自动配置.
 *
 * @author zengdegui
 * @since 2025/08/20
 */
@Slf4j
@Configuration
public class HttpClientIntegrationConfiguration {

    /**
     * 地址服务.
     *
     * @param httpClientExecutorStrategyFactory HTTP客户端执行器策略工厂（必填）
     * @return 地址服务
     */
    @Bean
    @ConditionalOnMissingBean
    public AddressService addressService(HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory) {
        return new AddressServiceImpl(httpClientExecutorStrategyFactory);
    }
}
