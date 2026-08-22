/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.service.kyc.impl;

import com.iwindplus.base.http.client.integration.domain.enums.KycProviderEnum;
import com.iwindplus.base.http.client.integration.factory.KycExecuteHandlerStrategyFactory;
import com.iwindplus.base.http.client.integration.service.kyc.KycService;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * KYC服务实现类.
 *
 * @author zengdegui
 * @since 2026/08/21
 */
@Slf4j
public class KycServiceImpl implements KycService {

    private final KycExecuteHandlerStrategyFactory kycStrategyFactory;

    public KycServiceImpl(
        KycExecuteHandlerStrategyFactory kycStrategyFactory) {
        this.kycStrategyFactory = kycStrategyFactory;
    }

    @Override
    public Optional<String> createVerification(Object request) {
        return this.kycStrategyFactory.createVerification(request);
    }

    @Override
    public Optional<String> createVerification(Object request, KycProviderEnum provider) {
        return this.kycStrategyFactory.createVerification(request, provider);
    }

    @Override
    public Optional<String> getVerificationStatus(KycProviderEnum provider, String verificationId) {
        return this.kycStrategyFactory.getVerificationStatus(provider, verificationId);
    }

    @Override
    public void handleWebhook(KycProviderEnum provider, String webhookData) {
        this.kycStrategyFactory.handleWebhook(provider, webhookData);
    }

    @Override
    public boolean verifyWebhookSignature(KycProviderEnum provider, String timestamp, String body, String signature) {
        return this.kycStrategyFactory.verifyWebhookSignature(provider, timestamp, body, signature);
    }

    @Override
    public List<KycProviderEnum> getAvailableProviders() {
        return this.kycStrategyFactory.getAvailableProviders();
    }
}
