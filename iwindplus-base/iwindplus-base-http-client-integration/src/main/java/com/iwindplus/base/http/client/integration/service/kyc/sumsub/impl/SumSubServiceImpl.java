/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.service.kyc.sumsub.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.http.client.executor.HttpClientExecutor;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.http.client.integration.domain.constant.SumSubConstant;
import com.iwindplus.base.http.client.integration.domain.dto.kyc.sumsub.SumSubAccessTokenDTO;
import com.iwindplus.base.http.client.integration.domain.dto.kyc.sumsub.SumSubApplicantDTO;
import com.iwindplus.base.http.client.integration.domain.property.KycProperty.ProviderConfig;
import com.iwindplus.base.http.client.integration.domain.vo.kyc.sumsub.SumSubAccessTokenVO;
import com.iwindplus.base.http.client.integration.domain.vo.kyc.sumsub.SumSubApplicantVO;
import com.iwindplus.base.http.client.integration.domain.vo.kyc.sumsub.SumSubDocumentCheckVO;
import com.iwindplus.base.http.client.integration.domain.vo.kyc.sumsub.SumSubDocumentVO;
import com.iwindplus.base.http.client.integration.factory.SumSubWebhookHandlerStrategyFactory;
import com.iwindplus.base.http.client.integration.service.kyc.sumsub.SumSubService;
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
public class SumSubServiceImpl extends AbstractBaseSumSubServiceImpl implements SumSubService {

    public SumSubServiceImpl(
        HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory,
        ProviderConfig config,
        SumSubWebhookHandlerStrategyFactory webhookHandlerFactory) {
        super(httpClientExecutorStrategyFactory, config, webhookHandlerFactory);
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
            : super.getConfig().getDefaultTokenTtl();
        queryParams.put(SumSubConstant.PARAM_TTL_IN_SECS, ttlInSecs);

        String levelName = request.getLevelName() != null
            ? request.getLevelName()
            : super.getConfig().getDefaultLevelName();
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
        if (request.getReview() == null && super.getConfig().getDefaultLevelName() != null) {
            request.setReview(super.getConfig().getDefaultLevelName());
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
}
