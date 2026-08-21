/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration;

import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.http.client.integration.domain.property.SumSubProperty;
import com.iwindplus.base.http.client.integration.factory.SumSubWebhookHandlerStrategyFactory;
import com.iwindplus.base.http.client.integration.listener.SumSubWebhookListenerProcessor;
import com.iwindplus.base.http.client.integration.service.SumSubService;
import com.iwindplus.base.http.client.integration.service.impl.SumSubServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SumSub服务自动配置.
 *
 * @author zengdegui
 * @since 2026/08/21
 */
@Slf4j
@ConditionalOnProperty(prefix = "sum-sub", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SumSubProperty.class)
@Configuration
public class SumSubAutoConfiguration {

    /**
     * SumSub Webhook监听器注解处理器.
     *
     * @return SumSubWebhookListenerProcessor
     */
    @Bean
    @ConditionalOnMissingBean
    public SumSubWebhookListenerProcessor sumSubWebhookListenerProcessor() {
        return new SumSubWebhookListenerProcessor();
    }

    /**
     * SumSub Webhook处理器策略工厂.
     *
     * @param listenerProcessor 注解处理器
     * @return SumSubWebhookHandlerStrategyFactory
     */
    @Bean
    @ConditionalOnMissingBean
    public SumSubWebhookHandlerStrategyFactory sumSubWebhookHandlerStrategyFactory(
        SumSubWebhookListenerProcessor listenerProcessor) {
        return new SumSubWebhookHandlerStrategyFactory(listenerProcessor);
    }

    /**
     * SumSub服务.
     *
     * @param httpClientExecutorStrategyFactory HTTP客户端执行器策略工厂（必填）
     * @param sumSubProperty                    SumSub配置属性（必填）
     * @param webhookHandlerFactory             Webhook处理器策略工厂（必填）
     * @return SumSub服务
     */
    @Bean
    @ConditionalOnMissingBean
    public SumSubService sumSubService(
        HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory,
        SumSubProperty sumSubProperty,
        SumSubWebhookHandlerStrategyFactory webhookHandlerFactory) {
        return new SumSubServiceImpl(httpClientExecutorStrategyFactory, sumSubProperty, webhookHandlerFactory);
    }
}
