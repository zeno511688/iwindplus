/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.executor.impl;

import cn.hutool.core.map.MapUtil;
import com.iwindplus.base.domain.constant.CommonConstant.NetWorkConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
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
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import reactor.core.publisher.Mono;

/**
 * WebClient执行器.
 *
 * @author zengdegui
 * @since 2026/01/19 23:35
 */
@Slf4j
public class WebClientExecutor extends AbstractHttpClientExecutor implements HttpClientExecutor {

    private final WebClient loadBalancedWebClient;
    private final WebClient webClient;

    public WebClientExecutor(
        HttpClientProperty property,
        HttpExecuteTemplate executeTemplate,
        ResponseExtractorStrategyFactory extractorStrategyFactory,
        DtpExecutor httpClientTaskExecutor,
        WebClient loadBalancedWebClient,
        WebClient webClient) {
        super(property, executeTemplate, extractorStrategyFactory, httpClientTaskExecutor);
        this.loadBalancedWebClient = loadBalancedWebClient;
        this.webClient = webClient;
    }

    @Override
    public HttpClientTypeEnum getClientType() {
        return HttpClientTypeEnum.WEB_CLIENT;
    }

    @Override
    protected HttpExecuteResultDTO doExecute(HttpRequestSpecDTO req) throws Exception {
        throw new UnsupportedOperationException("WebClientExecutor does not support sync execution.");
    }

    @Override
    protected CompletionStage<HttpExecuteResultDTO> doExecuteAsync(HttpRequestSpecDTO req) throws Exception {
        return executeInternal(req).toFuture();
    }

    private Mono<HttpExecuteResultDTO> executeInternal(HttpRequestSpecDTO req) {
        RequestBodySpec spec = buildRequestBodySpec(req);

        return spec.exchangeToMono(resp -> {
            int status = resp.statusCode().value();
            return resp.bodyToMono(String.class)
                .defaultIfEmpty(SymbolConstant.EMPTY_STR)
                .flatMap(body -> {
                    // 非2xx统一处理
                    if (!resp.statusCode().is2xxSuccessful()) {
                        return Mono.just(
                            HttpExecuteResultDTO.error(status, body)
                        );
                    }

                    return Mono.just(
                        HttpExecuteResultDTO.success(status, body)
                    );
                });
        });
    }

    private WebClient selectWebClient(String url) {
        if (url.startsWith(NetWorkConstant.LB_PREFIX)) {
            return loadBalancedWebClient;
        }
        return webClient;
    }

    private RequestBodySpec buildRequestBodySpec(HttpRequestSpecDTO req) {
        String uri = buildUrlQueryParams(req);

        RequestBodySpec spec = selectWebClient(req.getUrl())
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
        spec.bodyValue(body);
    }
}