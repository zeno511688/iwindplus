/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.executor.impl;

import cn.hutool.core.map.MapUtil;
import com.iwindplus.base.domain.constant.CommonConstant.NetWorkConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.http.client.domain.dto.HttpExecuteResultDTO;
import com.iwindplus.base.http.client.domain.dto.HttpRequestSpecDTO;
import com.iwindplus.base.http.client.domain.enums.HttpBodyTypeEnum;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.domain.property.HttpClientProperty;
import com.iwindplus.base.http.client.executor.HttpClientExecutor;
import com.iwindplus.base.http.client.factory.ResponseExtractorStrategyFactory;
import com.iwindplus.base.http.client.template.HttpExecuteTemplate;
import com.iwindplus.base.util.JacksonUtil;
import java.util.concurrent.CompletionStage;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestBodySpec;

/**
 * RestClient执行器.
 *
 * @author zengdegui
 * @since 2026/01/19 23:35
 */
@Slf4j
public class RestClientExecutor extends AbstractHttpClientExecutor implements HttpClientExecutor {

    private final RestClient loadBalancedRestClient;
    private final RestClient restClient;

    public RestClientExecutor(
        HttpClientProperty property,
        HttpExecuteTemplate executeTemplate,
        ResponseExtractorStrategyFactory extractorStrategyFactory,
        DtpExecutor httpClientTaskExecutor,
        RestClient loadBalancedRestClient,
        RestClient restClient) {
        super(property, executeTemplate, extractorStrategyFactory, httpClientTaskExecutor);
        this.loadBalancedRestClient = loadBalancedRestClient;
        this.restClient = restClient;
    }

    @Override
    public HttpClientTypeEnum getClientType() {
        return HttpClientTypeEnum.REST_CLIENT;
    }

    @Override
    protected HttpExecuteResultDTO doExecute(HttpRequestSpecDTO req) throws Exception {
        RequestBodySpec spec = buildRequestSpec(req);
        ResponseEntity<String> entity = spec.retrieve().toEntity(String.class);
        final int code = entity.getStatusCode().value();
        if (code >= 200 && code < 300) {
            return HttpExecuteResultDTO.success(code, entity.getBody());
        }
        return HttpExecuteResultDTO.error(code, entity.getBody());
    }

    @Override
    protected CompletionStage<HttpExecuteResultDTO> doExecuteAsync(HttpRequestSpecDTO req) throws Exception {
        throw new UnsupportedOperationException("RestClientExecutor does not support async execution.");
    }

    private RequestBodySpec buildRequestSpec(HttpRequestSpecDTO req) {
        String uri = buildUrlQueryParams(req);
        RestClient client = resolveRestClient(req.getUrl());

        RequestBodySpec spec = client
            .method(HttpMethod.valueOf(req.getMethod()))
            .uri(uri)
            .headers(headers -> {
                if (MapUtil.isNotEmpty(req.getHeaders())) {
                    headers.setAll(req.getHeaders());
                }
            });

        applyBody(spec, req);
        return spec;
    }

    private RestClient resolveRestClient(String url) {
        return url.startsWith(NetWorkConstant.LB_PREFIX)
            ? loadBalancedRestClient
            : restClient;
    }

    private void applyBody(RequestBodySpec spec, HttpRequestSpecDTO req) {
        HttpBodyTypeEnum bodyType = req.getBodyType();
        if (bodyType == null || bodyType == HttpBodyTypeEnum.NONE) {
            return;
        }

        MediaType mediaType;
        Object body;

        switch (bodyType) {
            case JSON:
                mediaType = MediaType.APPLICATION_JSON;
                body = req.getBody();
                if (body instanceof String str) {
                    body = str;
                } else {
                    body = JacksonUtil.toJsonStr(body);
                }
                break;

            case FORM:
                mediaType = MediaType.APPLICATION_FORM_URLENCODED;
                body = buildForm(req.getForm());
                break;

            case MULTIPART:
                mediaType = MediaType.MULTIPART_FORM_DATA;
                body = buildMultipart(req.getForm(), req.getFiles());
                break;

            default:
                throw new BizException(
                    BizCodeEnum.UNSUPPORTED_TYPE,
                    new Object[]{bodyType.getDesc()}
                );
        }

        spec.contentType(mediaType);
        spec.body(body);
    }
}