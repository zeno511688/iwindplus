/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration;

import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.http.client.integration.domain.enums.KycProviderEnum;
import com.iwindplus.base.http.client.integration.domain.property.KycProperty;
import com.iwindplus.base.http.client.integration.factory.KycExecuteHandlerStrategyFactory;
import com.iwindplus.base.http.client.integration.factory.SumSubWebhookHandlerStrategyFactory;
import com.iwindplus.base.http.client.integration.listener.SumSubWebhookListenerProcessor;
import com.iwindplus.base.http.client.integration.service.kyc.KycService;
import com.iwindplus.base.http.client.integration.service.kyc.impl.KycServiceImpl;
import com.iwindplus.base.http.client.integration.service.kyc.sumsub.SumSubService;
import com.iwindplus.base.http.client.integration.service.kyc.sumsub.impl.SumSubServiceImpl;
import com.iwindplus.base.http.client.integration.support.kyc.KycExecuteHandler;
import com.iwindplus.base.http.client.integration.support.kyc.impl.SumSubKycExecuteHandler;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * KYC服务自动配置.
 *
 * @author zengdegui
 * @since 2026/08/21
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(KycProperty.class)
@ConditionalOnProperty(prefix = "kyc", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KycAutoConfiguration {

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
     * @return SumSubWebhookHandlerStrategyFactory
     */
    @Bean
    @ConditionalOnMissingBean
    public SumSubWebhookHandlerStrategyFactory sumSubWebhookHandlerStrategyFactory(
        SumSubWebhookListenerProcessor listenerProcessor) {
        log.info("Initializing SumSubWebhookHandlerStrategyFactory");
        return new SumSubWebhookHandlerStrategyFactory(listenerProcessor);
    }

    /**
     * SumSub服务实现.
     *
     * @param httpClientExecutorStrategyFactory HTTP客户端执行器策略工厂
     * @param kycProperty                       KYC配置属性
     * @param webhookHandlerFactory             Webhook处理器策略工厂
     * @return SumSubService
     */
    @Bean
    @ConditionalOnMissingBean
    public SumSubService sumSubService(
        HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory,
        KycProperty kycProperty,
        SumSubWebhookHandlerStrategyFactory webhookHandlerFactory) {
        KycProperty.ProviderConfig config = kycProperty.getProviderConfig(KycProviderEnum.SUMSUB);
        if (config == null) {
            log.info("SumSub provider is disabled");
            return null;
        }
        log.info("Initializing SumSubService");
        return new SumSubServiceImpl(httpClientExecutorStrategyFactory, config, webhookHandlerFactory);
    }

    /**
     * SumSub KYC执行策略.
     *
     * @param sumSubService SumSub服务
     * @param kycProperty   KYC配置属性
     * @return SumSubKycExecuteHandler
     */
    @Bean
    @ConditionalOnMissingBean(name = "sumSubKycExecuteHandler")
    public KycExecuteHandler sumSubKycExecuteHandler(
            SumSubService sumSubService,
            KycProperty kycProperty) {
        KycProperty.ProviderConfig config = kycProperty.getProviderConfig(KycProviderEnum.SUMSUB);
        if (config == null) {
            log.info("SumSub KYC provider is disabled");
            return null;
        }
        log.info("Initializing SumSub KYC execute handler");
        return new SumSubKycExecuteHandler(sumSubService, config);
    }

    /**
     * KYC服务策略工厂.
     *
     * @param kycProperty KYC配置属性
     * @param strategies  策略列表
     * @return KycExecuteHandlerStrategyFactory
     */
    @Bean
    @ConditionalOnMissingBean
    public KycExecuteHandlerStrategyFactory kycExecuteHandlerStrategyFactory(
            KycProperty kycProperty,
            List<KycExecuteHandler> strategies) {
        // 过滤掉 null 的策略
        List<KycExecuteHandler> validStrategies = strategies.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Initializing KycExecuteHandlerStrategyFactory with {} strategies", validStrategies.size());
        return new KycExecuteHandlerStrategyFactory(kycProperty, validStrategies);
    }

    /**
     * KYC服务.
     *
     * @param kycExecuteHandlerStrategyFactory KYC服务策略工厂
     * @return KycService
     */
    @Bean
    @ConditionalOnMissingBean
    public KycService kycService(
            KycExecuteHandlerStrategyFactory kycExecuteHandlerStrategyFactory) {
        log.info("Initializing KycService");
        return new KycServiceImpl(kycExecuteHandlerStrategyFactory);
    }
}
