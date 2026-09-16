/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sumsub;

import com.iwindplus.base.http.client.factory.HttpClientExecuteHandlerFactory;
import com.iwindplus.base.sumsub.domain.property.SumSubProperty;
import com.iwindplus.base.sumsub.domain.property.SumSubProperty.BaseConfig;
import com.iwindplus.base.sumsub.factory.SumSubExecuteHandlerFactory;
import com.iwindplus.base.sumsub.factory.SumSubWebhookHandlerFactory;
import com.iwindplus.base.sumsub.listener.SumSubWebhookListenerProcessor;
import com.iwindplus.base.sumsub.support.SumSubExecuteHandler;
import com.iwindplus.base.sumsub.support.impl.SumSubExecuteHandlerImpl;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
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
 * @since 2026/9/7
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "sumsub", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SumSubProperty.class)
public class SumSubConfiguration {

    /**
     * SumSub Webhook监听器注解处理器.
     *
     * @return SumSubWebhookListenerProcessor
     */
    @Bean
    @ConditionalOnMissingBean
    public SumSubWebhookListenerProcessor sumSubWebhookListenerProcessor() {
        log.info("Initializing SumSubWebhookListenerProcessor");
        return new SumSubWebhookListenerProcessor();
    }

    /**
     * SumSub Webhook处理器策略工厂.
     *
     * @param listenerProcessor 注解处理器
     * @return SumSubWebhookHandlerFactory
     */
    @Bean
    @ConditionalOnMissingBean
    public SumSubWebhookHandlerFactory sumSubWebhookHandlerFactory(
        SumSubWebhookListenerProcessor listenerProcessor) {
        log.info("Initializing SumSubWebhookHandlerFactory");
        return new SumSubWebhookHandlerFactory(listenerProcessor);
    }

    /**
     * SumSub策略工厂.
     *
     * @param property                        SumSub属性配置
     * @param httpClientExecuteHandlerFactory HTTP客户端执行器策略工厂
     * @param webhookHandlerFactory           Webhook处理器策略工厂
     * @return 策略工厂
     */
    @Bean
    public SumSubExecuteHandlerFactory sumSubExecuteHandlerFactory(
        SumSubProperty property,
        HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory,
        SumSubWebhookHandlerFactory webhookHandlerFactory) {
        List<SumSubExecuteHandler> handlers = new ArrayList<>(10);
        handlers.addAll(
            this.buildHandlers(property.getConfigs(),
                config -> new SumSubExecuteHandlerImpl(config, httpClientExecuteHandlerFactory, webhookHandlerFactory))
        );
        SumSubExecuteHandlerFactory factory = new SumSubExecuteHandlerFactory(property, handlers);
        log.info("SumSubExecuteHandlerFactory={}", factory);
        return factory;
    }

    /**
     * 根据配置列表构建策略实例列表（过滤未启用配置并按编码去重）.
     *
     * @param configs        配置列表
     * @param handlerFactory 策略实例工厂
     * @param <C>            配置类型
     * @return 策略实例列表
     */
    private <C extends BaseConfig> List<SumSubExecuteHandler> buildHandlers(
        List<C> configs,
        Function<C, SumSubExecuteHandler> handlerFactory) {
        List<SumSubExecuteHandler> handlers = new ArrayList<>(10);
        Set<String> codes = new HashSet<>(16);
        configs.stream()
            .filter(config -> Boolean.TRUE.equals(config.getEnabled()))
            .filter(config -> codes.add(config.getCode()))
            .forEach(config -> {
                log.info("Initializing SumSub strategy [code={}]", config.getCode());
                handlers.add(handlerFactory.apply(config));
            });
        return handlers;
    }
}
