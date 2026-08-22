/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.support.kyc.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.iwindplus.base.http.client.integration.domain.dto.kyc.sumsub.SumSubApplicantDTO;
import com.iwindplus.base.http.client.integration.domain.dto.kyc.sumsub.SumSubWebhookDTO;
import com.iwindplus.base.http.client.integration.domain.enums.KycProviderEnum;
import com.iwindplus.base.http.client.integration.domain.property.KycProperty;
import com.iwindplus.base.http.client.integration.domain.vo.kyc.sumsub.SumSubApplicantVO;
import com.iwindplus.base.http.client.integration.service.kyc.sumsub.SumSubService;
import com.iwindplus.base.http.client.integration.support.kyc.KycExecuteHandler;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * SumSub KYC服务执行策略.
 *
 * @author zengdegui
 * @since 2026/08/21
 */
@Slf4j
public class SumSubKycExecuteHandler implements KycExecuteHandler {

    @Getter
    private final KycProviderEnum provider = KycProviderEnum.SUMSUB;

    private final SumSubService sumSubService;
    private final KycProperty.ProviderConfig config;

    public SumSubKycExecuteHandler(
            SumSubService sumSubService,
            KycProperty.ProviderConfig config) {
        this.sumSubService = sumSubService;
        this.config = config;
    }

    @Override
    public Optional<String> createVerification(Object request) {
        try {
            return doCreateVerification(request);
        } catch (Exception e) {
            log.warn("KYC verification creation failed [provider={}]: error={}", provider.getName(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> doCreateVerification(Object request) {
        String apiKey = this.config.getApiKey();
        String apiSecret = this.config.getApiSecret();

        if (StrUtil.isBlank(apiKey) || StrUtil.isBlank(apiSecret)) {
            log.warn("SumSub apiKey or apiSecret is not configured");
            return Optional.empty();
        }

        // 解析请求参数
        SumSubApplicantDTO applicantRequest;
        try {
            if (request instanceof SumSubApplicantDTO) {
                applicantRequest = (SumSubApplicantDTO) request;
            } else if (request instanceof String) {
                applicantRequest = JSONUtil.toBean((String) request, SumSubApplicantDTO.class);
            } else {
                applicantRequest = JSONUtil.toBean(JSONUtil.toJsonStr(request), SumSubApplicantDTO.class);
            }
        } catch (Exception e) {
            log.error("Failed to parse SumSub applicant request: error={}", e.getMessage());
            return Optional.empty();
        }

        // 调用SumSub服务创建申请人
        Optional<SumSubApplicantVO> result = this.sumSubService.createApplicant(applicantRequest);

        return result.map(vo -> JSONUtil.toJsonStr(vo));
    }

    @Override
    public Optional<String> getVerificationStatus(String verificationId) {
        try {
            return doGetVerificationStatus(verificationId);
        } catch (Exception e) {
            log.warn("KYC verification status query failed [provider={}, verificationId={}]: error={}",
                    provider.getName(), verificationId, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> doGetVerificationStatus(String verificationId) {
        if (StrUtil.isBlank(verificationId)) {
            log.warn("SumSub verificationId is blank");
            return Optional.empty();
        }

        // 调用SumSub服务获取申请人信息
        Optional<SumSubApplicantVO> result = this.sumSubService.getApplicant(verificationId);

        return result.map(vo -> JSONUtil.toJsonStr(vo));
    }

    @Override
    public void handleWebhook(String webhookData) {
        try {
            SumSubWebhookDTO webhookDTO = JSONUtil.toBean(webhookData, SumSubWebhookDTO.class);
            this.sumSubService.handleWebhook(webhookDTO);
        } catch (Exception e) {
            log.error("Failed to handle SumSub webhook: error={}", e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyWebhookSignature(String timestamp, String body, String signature) {
        try {
            return this.sumSubService.verifyWebhookSignature(timestamp, body, signature);
        } catch (Exception e) {
            log.error("Failed to verify SumSub webhook signature: error={}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isHealthy() {
        // 检查必要的配置是否存在
        return StrUtil.isNotBlank(this.config.getApiKey())
                && StrUtil.isNotBlank(this.config.getApiSecret());
    }
}
