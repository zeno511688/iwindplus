/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.http.client.executor.HttpClientExecutor;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.http.client.integration.domain.constant.SumSubConstant;
import com.iwindplus.base.http.client.integration.domain.dto.sumsub.SumSubAccessTokenDTO;
import com.iwindplus.base.http.client.integration.domain.dto.sumsub.SumSubApplicantDTO;
import com.iwindplus.base.http.client.integration.domain.dto.sumsub.SumSubWebhookDTO;
import com.iwindplus.base.http.client.integration.domain.property.SumSubProperty;
import com.iwindplus.base.http.client.integration.domain.vo.sumsub.SumSubAccessTokenVO;
import com.iwindplus.base.http.client.integration.domain.vo.sumsub.SumSubApplicantVO;
import com.iwindplus.base.http.client.integration.domain.vo.sumsub.SumSubDocumentCheckVO;
import com.iwindplus.base.http.client.integration.domain.vo.sumsub.SumSubDocumentVO;
import com.iwindplus.base.http.client.integration.factory.SumSubWebhookHandlerStrategyFactory;
import com.iwindplus.base.http.client.integration.service.SumSubService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

/**
 * SumSub服务实现类.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
@Slf4j
public class SumSubServiceImpl extends AbstractBaseSubSubServiceImpl implements SumSubService {

    private final SumSubWebhookHandlerStrategyFactory webhookHandlerFactory;

    public SumSubServiceImpl(
        HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory,
        SumSubProperty sumSubProperty,
        SumSubWebhookHandlerStrategyFactory webhookHandlerFactory) {
        super(httpClientExecutorStrategyFactory, sumSubProperty);
        this.webhookHandlerFactory = webhookHandlerFactory;
    }

    @Override
    public Optional<SumSubAccessTokenVO> getAccessToken(SumSubAccessTokenDTO request) {
        HttpClientExecutor executor = super.getHttpClientExecutorStrategyFactory().getDefaultHttpClientExecutor();
        Map<String, String> headers = this.buildAuthHeaders(HttpMethod.POST.name(), SumSubConstant.Url.ACCESS_TOKEN, null);
        Map<String, Object> queryParams = new HashMap<>(4);
        queryParams.put(SumSubConstant.PARAM_USER_ID, request.getExternalUserId());
        
        // 使用请求中的值，如果为null则使用配置中的默认值
        Integer ttlInSecs = request.getTtlInSecs() != null 
            ? request.getTtlInSecs() 
            : super.getProperty().getDefaultTokenTtl();
        queryParams.put(SumSubConstant.PARAM_TTL_IN_SECS, ttlInSecs);
        
        String levelName = request.getLevelName() != null 
            ? request.getLevelName() 
            : super.getProperty().getDefaultLevelName();
        queryParams.put(SumSubConstant.PARAM_LEVEL_NAME, levelName);
        
        SumSubAccessTokenVO response = executor.post(
            SumSubConstant.Url.ACCESS_TOKEN,
            null,
            headers,
            SumSubAccessTokenVO.class
        );
        return Optional.ofNullable(response);
    }

    @Override
    public Optional<SumSubApplicantVO> createApplicant(SumSubApplicantDTO request) {
        HttpClientExecutor executor = super.getHttpClientExecutorStrategyFactory().getDefaultHttpClientExecutor();
        
        // 如果请求中没有指定审核级别，使用配置中的默认值
        if (request.getReview() == null && super.getProperty().getDefaultLevelName() != null) {
            request.setReview(super.getProperty().getDefaultLevelName());
        }
        
        Map<String, String> headers = this.buildAuthHeaders(HttpMethod.POST.name(), SumSubConstant.Url.APPLICANT, request);
        SumSubApplicantVO response = executor.post(
            SumSubConstant.Url.APPLICANT,
            request,
            headers,
            SumSubApplicantVO.class
        );
        return Optional.ofNullable(response);
    }

    @Override
    public Optional<SumSubApplicantVO> getApplicant(String applicantId) {
        HttpClientExecutor executor = super.getHttpClientExecutorStrategyFactory().getDefaultHttpClientExecutor();
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
        HttpClientExecutor executor = super.getHttpClientExecutorStrategyFactory().getDefaultHttpClientExecutor();
        Map<String, String> queryParams = new HashMap<>(4);
        queryParams.put(SumSubConstant.PARAM_EXTERNAL_USER_ID, externalUserId);
        Map<String, String> headers = this.buildAuthHeaders(HttpMethod.GET.name(), SumSubConstant.Url.APPLICANT, null);
        SumSubApplicantVO response = executor.get(
            SumSubConstant.Url.APPLICANT,
            queryParams,
            headers,
            SumSubApplicantVO.class
        );
        return Optional.ofNullable(response);
    }

    @Override
    public Optional<SumSubApplicantVO> updateApplicant(String applicantId, SumSubApplicantDTO request) {
        HttpClientExecutor executor = super.getHttpClientExecutorStrategyFactory().getDefaultHttpClientExecutor();
        String url = String.format(SumSubConstant.Url.APPLICANT_DETAIL, applicantId);
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
    public Optional<SumSubApplicantVO> resetApplicant(String applicantId) {
        HttpClientExecutor executor = super.getHttpClientExecutorStrategyFactory().getDefaultHttpClientExecutor();
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
        HttpClientExecutor executor = super.getHttpClientExecutorStrategyFactory().getDefaultHttpClientExecutor();
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
        HttpClientExecutor executor = super.getHttpClientExecutorStrategyFactory().getDefaultHttpClientExecutor();
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
        HttpClientExecutor executor = super.getHttpClientExecutorStrategyFactory().getDefaultHttpClientExecutor();
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

    @Override
    public void handleWebhook(SumSubWebhookDTO webhookData) {
        log.info("Received SumSub webhook: type={}, applicantId={}, externalUserId={}",
            webhookData.getType(), webhookData.getApplicantId(), webhookData.getExternalUserId());

        // 根据不同的Webhook类型进行处理
        String webhookType = webhookData.getType();
        if (webhookType == null) {
            log.warn("Webhook type is null, skipping processing");
            return;
        }

        // 使用策略工厂获取对应的处理器
        try {
            webhookHandlerFactory.getHandler(webhookType).accept(webhookData);
        } catch (Exception e) {
            log.error("Failed to handle webhook: type={}, error={}", webhookType, e.getMessage(), e);
        }
    }
}
