/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.factory;

import com.iwindplus.base.http.client.integration.domain.enums.KycProviderEnum;
import com.iwindplus.base.http.client.integration.domain.property.KycProperty;
import com.iwindplus.base.http.client.integration.support.kyc.KycExecuteHandler;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * KYC服务策略工厂.
 * 负责管理和路由KYC服务策略，支持动态路由和故障转移.
 *
 * @author zengdegui
 * @since 2026/08/21
 */
@Slf4j
public class KycExecuteHandlerStrategyFactory {

    private final KycProperty property;
    private final Map<KycProviderEnum, KycExecuteHandler> strategyMap = new ConcurrentHashMap<>();

    public KycExecuteHandlerStrategyFactory(KycProperty property, List<KycExecuteHandler> strategies) {
        this.property = property;
        // 初始化策略映射
        strategies.forEach(strategy -> strategyMap.put(strategy.getProvider(), strategy));
    }

    /**
     * 创建验证流程（自动故障转移）.
     *
     * @param request 验证流程请求参数
     * @return 验证流程响应
     */
    public Optional<String> createVerification(Object request) {
        if (!Boolean.TRUE.equals(this.property.getEnabled())) {
            log.warn("KYC service is disabled");
            return Optional.empty();
        }

        // 获取启用的提供商配置（按优先级排序）
        Map<String, KycProperty.ProviderConfig> enabledProviders = this.property.getEnabledProviders();

        if (enabledProviders.isEmpty()) {
            log.warn("No enabled KYC providers configured");
            return Optional.empty();
        }

        // 按优先级依次尝试
        for (Map.Entry<String, KycProperty.ProviderConfig> entry : enabledProviders.entrySet()) {
            String providerCode = entry.getKey();
            KycProviderEnum provider = KycProviderEnum.getByCode(providerCode);
            if (provider == null) {
                log.warn("Invalid provider code: {}", providerCode);
                continue;
            }

            KycExecuteHandler strategy = this.strategyMap.get(provider);
            if (strategy == null) {
                log.warn("No strategy found for provider: {}", provider.getName());
                continue;
            }

            // 健康检查
            if (!strategy.isHealthy()) {
                log.warn("Provider {} is not healthy, skipping", provider.getName());
                continue;
            }

            // 尝试创建验证流程
            Optional<String> result = strategy.createVerification(request);
            if (result.isPresent()) {
                log.info("KYC verification created successfully [provider={}]", provider.getName());
                return result;
            }

            log.warn("KYC verification creation failed [provider={}], trying next provider", provider.getName());
        }

        log.error("All KYC providers failed for verification creation");
        return Optional.empty();
    }

    /**
     * 使用指定提供商创建验证流程.
     *
     * @param request  验证流程请求参数
     * @param provider 提供商
     * @return 验证流程响应
     */
    public Optional<String> createVerification(Object request, KycProviderEnum provider) {
        if (!Boolean.TRUE.equals(this.property.getEnabled())) {
            log.warn("KYC service is disabled");
            return Optional.empty();
        }

        KycExecuteHandler strategy = this.strategyMap.get(provider);
        if (strategy == null) {
            log.warn("No strategy found for provider: {}", provider.getName());
            return Optional.empty();
        }

        return strategy.createVerification(request);
    }

    /**
     * 查询验证状态.
     *
     * @param provider       提供商
     * @param verificationId 验证流程ID
     * @return 验证状态响应
     */
    public Optional<String> getVerificationStatus(KycProviderEnum provider, String verificationId) {
        if (!Boolean.TRUE.equals(this.property.getEnabled())) {
            log.warn("KYC service is disabled");
            return Optional.empty();
        }

        KycExecuteHandler strategy = this.strategyMap.get(provider);
        if (strategy == null) {
            log.warn("No strategy found for provider: {}", provider.getName());
            return Optional.empty();
        }

        return strategy.getVerificationStatus(verificationId);
    }

    /**
     * 处理Webhook回调.
     *
     * @param provider    提供商
     * @param webhookData Webhook数据
     */
    public void handleWebhook(KycProviderEnum provider, String webhookData) {
        if (!Boolean.TRUE.equals(this.property.getEnabled())) {
            log.warn("KYC service is disabled");
            return;
        }

        KycExecuteHandler strategy = this.strategyMap.get(provider);
        if (strategy == null) {
            log.warn("No strategy found for provider: {}", provider.getName());
            return;
        }

        strategy.handleWebhook(webhookData);
    }

    /**
     * 验证Webhook签名.
     *
     * @param provider  提供商
     * @param timestamp 时间戳
     * @param body      请求体
     * @param signature 签名
     * @return 验证结果
     */
    public boolean verifyWebhookSignature(KycProviderEnum provider, String timestamp, String body, String signature) {
        if (!Boolean.TRUE.equals(this.property.getEnabled())) {
            log.warn("KYC service is disabled");
            return false;
        }

        KycExecuteHandler strategy = this.strategyMap.get(provider);
        if (strategy == null) {
            log.warn("No strategy found for provider: {}", provider.getName());
            return false;
        }

        return strategy.verifyWebhookSignature(timestamp, body, signature);
    }

    /**
     * 获取所有可用的提供商.
     *
     * @return 提供商列表
     */
    public List<KycProviderEnum> getAvailableProviders() {
        return this.strategyMap.values().stream()
                .filter(KycExecuteHandler::isHealthy)
                .sorted(Comparator.comparingInt(KycExecuteHandler::getPriority))
                .map(KycExecuteHandler::getProvider)
                .collect(Collectors.toList());
    }

    /**
     * 获取提供商配置.
     *
     * @param provider 提供商
     * @return 配置
     */
    public KycProperty.ProviderConfig getProviderConfig(KycProviderEnum provider) {
        return this.property.getProviderConfig(provider);
    }

    /**
     * 获取指定提供商的策略.
     *
     * @param provider 提供商
     * @return 策略
     */
    public Optional<KycExecuteHandler> getStrategy(KycProviderEnum provider) {
        return Optional.ofNullable(this.strategyMap.get(provider));
    }
}
