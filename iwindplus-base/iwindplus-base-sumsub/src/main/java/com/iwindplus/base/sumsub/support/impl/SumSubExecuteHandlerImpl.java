/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sumsub.support.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.http.client.domain.dto.HttpRequestSpecDTO;
import com.iwindplus.base.http.client.factory.HttpClientExecuteHandlerFactory;
import com.iwindplus.base.http.client.support.HttpClientExecuteHandler;
import com.iwindplus.base.sumsub.domain.constant.SumSubConstant;
import com.iwindplus.base.sumsub.domain.dto.SumSubAccessTokenDTO;
import com.iwindplus.base.sumsub.domain.dto.SumSubApplicantDTO;
import com.iwindplus.base.sumsub.domain.dto.SumSubWebhookDTO;
import com.iwindplus.base.sumsub.domain.property.SumSubProperty.SumSubConfig;
import com.iwindplus.base.sumsub.domain.vo.SumSubAccessTokenVO;
import com.iwindplus.base.sumsub.domain.vo.SumSubApplicantVO;
import com.iwindplus.base.sumsub.domain.vo.SumSubDocumentCheckVO;
import com.iwindplus.base.sumsub.domain.vo.SumSubDocumentVO;
import com.iwindplus.base.sumsub.factory.SumSubWebhookHandlerFactory;
import com.iwindplus.base.sumsub.service.impl.AbstractBaseServiceImpl;
import com.iwindplus.base.sumsub.support.SumSubExecuteHandler;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.HttpMethod;

/**
 * SumSub策略实现类.
 *
 * @author zengdegui
 * @since 2026/9/7
 */
@Slf4j
public class SumSubExecuteHandlerImpl extends AbstractBaseServiceImpl<SumSubConfig> implements SumSubExecuteHandler {

    private final HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory;
    private final SumSubWebhookHandlerFactory webhookHandlerFactory;

    /**
     * 构造函数.
     *
     * @param config                          SumSub配置
     * @param httpClientExecuteHandlerFactory HTTP客户端执行器策略工厂
     * @param webhookHandlerFactory           Webhook处理器策略工厂
     */
    public SumSubExecuteHandlerImpl(
        SumSubConfig config,
        HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory,
        SumSubWebhookHandlerFactory webhookHandlerFactory) {
        this.httpClientExecuteHandlerFactory = httpClientExecuteHandlerFactory;
        this.webhookHandlerFactory = webhookHandlerFactory;
        super.setConfig(config);
    }

    @Override
    public void handleWebhook(SumSubWebhookDTO webhookData) {
        log.info("Received SumSub webhook: type={}, applicantId={}, externalUserId={}",
            webhookData.getType(), webhookData.getApplicantId(), webhookData.getExternalUserId());

        String webhookType = webhookData.getType();
        if (webhookType == null) {
            log.warn("Webhook type is null, skipping processing");
            return;
        }

        try {
            webhookHandlerFactory.getHandler(webhookType).accept(webhookData);
        } catch (Exception e) {
            log.error("Failed to handle webhook: type={}, error={}", webhookType, e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyWebhookSignature(String timestamp, String body, String signature) {
        String secretKey = super.getConfig().getWebhookSecretKey();
        if (timestamp == null || body == null || signature == null || secretKey == null) {
            log.warn("Invalid signature verification parameters: timestamp={}, body={}, signature={}, secretKey={}",
                timestamp != null, body != null, signature != null, secretKey != null);
            return false;
        }

        try {
            String data = timestamp + body;
            byte[] expectedHmacBytes = this.hmacSha256(data, secretKey);
            byte[] actualHmacBytes = Hex.decodeHex(signature);

            // 使用常量时间比较，防止时间侧信道攻击
            boolean isValid = MessageDigest.isEqual(expectedHmacBytes, actualHmacBytes);

            if (!isValid) {
                // 日志中不输出完整签名，仅输出前后几位用于排查
                String expectedSignature = Hex.encodeHexString(expectedHmacBytes);
                log.warn("Signature verification failed: expected={}...{}, actual={}...{}",
                    expectedSignature.substring(0, Math.min(8, expectedSignature.length())),
                    expectedSignature.substring(Math.max(0, expectedSignature.length() - 8)),
                    signature.substring(0, Math.min(8, signature.length())),
                    signature.substring(Math.max(0, signature.length() - 8)));
            }

            return isValid;
        } catch (Exception e) {
            log.error("Webhook signature verification failed", e);
            throw new BizException(BizCodeEnum.INVALID_SIGN);
        }
    }

    @Override
    public Optional<SumSubAccessTokenVO> getAccessToken(SumSubAccessTokenDTO request) {
        HttpClientExecuteHandler executor = this.httpClientExecuteHandlerFactory.getDefaultHandler();

        // 使用请求中的值，如果为null则使用配置中的默认值
        Integer ttlInSecs = request.getTtlInSecs() != null
            ? request.getTtlInSecs()
            : super.getConfig().getDefaultTokenTtl();
        String levelName = request.getLevelName() != null
            ? request.getLevelName()
            : super.getConfig().getDefaultLevelName();

        // 构建请求体（SumSub 官方 API 使用 JSON body 传递参数）
        Map<String, Object> body = new HashMap<>(4);
        body.put(SumSubConstant.PARAM_USER_ID, request.getExternalUserId());
        body.put(SumSubConstant.PARAM_TTL_IN_SECS, ttlInSecs);
        body.put(SumSubConstant.PARAM_LEVEL_NAME, levelName);

        // 签名时使用不含 query 的 URL（参数在 body 中）
        Map<String, String> headers = this.buildAuthHeaders(
            HttpMethod.POST.name(), SumSubConstant.Url.ACCESS_TOKEN, body);

        SumSubAccessTokenVO response = executor.post(
            SumSubConstant.Url.ACCESS_TOKEN,
            body,
            headers,
            SumSubAccessTokenVO.class
        );
        return Optional.ofNullable(response);
    }

    @Override
    public Optional<SumSubApplicantVO> createApplicant(SumSubApplicantDTO request) {
        HttpClientExecuteHandler executor = this.httpClientExecuteHandlerFactory.getDefaultHandler();

        // levelName 通过 query 参数传递（官方规范，required），需拼接到 URL 并参与签名
        // 官方文档要求：level name 包含保留字符时必须 URL 编码，否则会导致签名不匹配
        String levelName = super.getConfig().getDefaultLevelName();
        String url = SumSubConstant.Url.APPLICANT;
        if (levelName != null) {
            url = url + "?" + SumSubConstant.PARAM_LEVEL_NAME + "="
                + URLEncoder.encode(levelName, StandardCharsets.UTF_8);
        }

        Map<String, String> headers = this.buildAuthHeaders(HttpMethod.POST.name(), url, request);
        SumSubApplicantVO response = executor.post(
            url,
            request,
            headers,
            SumSubApplicantVO.class
        );
        return Optional.ofNullable(response);
    }

    @Override
    public Optional<SumSubApplicantVO> getApplicant(String applicantId) {
        HttpClientExecuteHandler executor = this.httpClientExecuteHandlerFactory.getDefaultHandler();
        String url = String.format(SumSubConstant.Url.APPLICANT_DETAIL, applicantId);
        Map<String, String> headers = this.buildAuthHeaders(HttpMethod.GET.name(), url, null);
        SumSubApplicantVO response = executor.get(
            url,
            null,
            headers,
            SumSubApplicantVO.class
        );
        return Optional.ofNullable(response);
    }

    @Override
    public Optional<SumSubApplicantVO> getApplicantByExternalUserId(String externalUserId) {
        HttpClientExecuteHandler executor = this.httpClientExecuteHandlerFactory.getDefaultHandler();
        // 官方规范：使用特殊路径语法，而非 query 参数
        String url = String.format(SumSubConstant.Url.APPLICANT_BY_EXTERNAL_USER_ID, externalUserId);
        Map<String, String> headers = this.buildAuthHeaders(HttpMethod.GET.name(), url, null);
        SumSubApplicantVO response = executor.get(
            url,
            null,
            headers,
            SumSubApplicantVO.class
        );
        return Optional.ofNullable(response);
    }

    @Override
    public Optional<SumSubApplicantVO> updateApplicant(String applicantId, SumSubApplicantDTO request) {
        HttpClientExecuteHandler executor = this.httpClientExecuteHandlerFactory.getDefaultHandler();
        String url = String.format(SumSubConstant.Url.APPLICANT_DETAIL, applicantId);
        // 官方规范：更新申请人使用 PATCH 方法
        Map<String, String> headers = this.buildAuthHeaders(HttpMethod.PATCH.name(), url, request);
        SumSubApplicantVO response = executor.exchange(
            HttpRequestSpecDTO.json(HttpMethod.PATCH.name(), url, headers, request),
            SumSubApplicantVO.class
        );
        return Optional.ofNullable(response);
    }

    @Override
    public Optional<SumSubApplicantVO> resetApplicant(String applicantId) {
        HttpClientExecuteHandler executor = this.httpClientExecuteHandlerFactory.getDefaultHandler();
        String url = String.format(SumSubConstant.Url.APPLICANT_RESET, applicantId);
        Map<String, String> headers = this.buildAuthHeaders(HttpMethod.POST.name(), url, null);
        SumSubApplicantVO response = executor.post(
            url,
            null,
            headers,
            SumSubApplicantVO.class
        );
        return Optional.ofNullable(response);
    }

    @Override
    public Optional<List<SumSubDocumentVO>> getDocuments(String applicantId) {
        HttpClientExecuteHandler executor = this.httpClientExecuteHandlerFactory.getDefaultHandler();
        String url = String.format(SumSubConstant.Url.APPLICANT_DOCUMENTS, applicantId);
        Map<String, String> headers = this.buildAuthHeaders(HttpMethod.GET.name(), url, null);
        List<SumSubDocumentVO> response = executor.get(
            url,
            null,
            headers,
            new TypeReference<>() {
            }
        );
        return Optional.ofNullable(response);
    }

    @Override
    public Optional<SumSubDocumentVO> getDocument(String documentId) {
        HttpClientExecuteHandler executor = this.httpClientExecuteHandlerFactory.getDefaultHandler();
        String url = String.format(SumSubConstant.Url.DOCUMENT_DETAIL, documentId);
        Map<String, String> headers = this.buildAuthHeaders(HttpMethod.GET.name(), url, null);
        SumSubDocumentVO response = executor.get(
            url,
            null,
            headers,
            SumSubDocumentVO.class
        );
        return Optional.ofNullable(response);
    }

    @Override
    public Optional<List<SumSubDocumentCheckVO>> getDocumentChecks(String documentId) {
        HttpClientExecuteHandler executor = this.httpClientExecuteHandlerFactory.getDefaultHandler();
        final String url = String.format(
            SumSubConstant.Url.DOCUMENT_CHECKS,
            documentId
        );
        Map<String, String> headers = this.buildAuthHeaders(HttpMethod.GET.name(), url, null);
        List<SumSubDocumentCheckVO> response = executor.get(
            url,
            null,
            headers,
            new TypeReference<>() {
            }
        );
        return Optional.ofNullable(response);
    }

}
