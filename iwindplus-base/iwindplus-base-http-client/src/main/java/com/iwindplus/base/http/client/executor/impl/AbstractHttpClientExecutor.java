/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.executor.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.constant.CommonConstant.FileConstant;
import com.iwindplus.base.http.client.domain.dto.HttpExecuteResultDTO;
import com.iwindplus.base.http.client.domain.dto.HttpRequestSpecDTO;
import com.iwindplus.base.http.client.domain.property.HttpClientProperty;
import com.iwindplus.base.http.client.executor.HttpClientExecutor;
import com.iwindplus.base.http.client.factory.ResponseExtractorStrategyFactory;
import com.iwindplus.base.http.client.template.HttpExecuteTemplate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 抽象http执行器.
 *
 * @author zengdegui
 * @since 2026/01/20 01:02
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AbstractHttpClientExecutor implements HttpClientExecutor {

    private final HttpClientProperty property;
    private final HttpExecuteTemplate executeTemplate;
    private final ResponseExtractorStrategyFactory extractorStrategyFactory;
    private final DtpExecutor httpClientTaskExecutor;

    @Override
    public <T> T exchange(HttpRequestSpecDTO request, Class<T> responseType) {
        HttpExecuteResultDTO result = executeInternal(request);
        return extractorStrategyFactory.extract(result, responseType);
    }

    @Override
    public <T> T exchange(HttpRequestSpecDTO request, TypeReference<T> typeReference) {
        HttpExecuteResultDTO result = executeInternal(request);
        return extractorStrategyFactory.extract(result, typeReference);
    }

    @Override
    public <T> CompletionStage<T> exchangeAsync(HttpRequestSpecDTO request, Class<T> responseType) {
        return executeAsyncInternal(request)
            .thenApplyAsync(result -> extractorStrategyFactory.extract(result, responseType), httpClientTaskExecutor);
    }

    @Override
    public <T> CompletionStage<T> exchangeAsync(HttpRequestSpecDTO request, TypeReference<T> typeReference) {
        return executeAsyncInternal(request)
            .thenApplyAsync(result -> extractorStrategyFactory.extract(result, typeReference), httpClientTaskExecutor);
    }

    /**
     * 构建URL参数.
     *
     * @param req 请求参数
     * @return String
     */
    protected String buildUrlQueryParams(HttpRequestSpecDTO req) {
        String uri = UriComponentsBuilder
            .fromUriString(req.getUrl())
            .queryParams(buildMapParams(req.getQuery()))
            .build(true)
            .toUriString();
        return uri;
    }

    /**
     * 构建Map参数.
     *
     * @param params 参数
     * @return MultiValueMap<String, String>
     */
    protected MultiValueMap<String, String> buildMapParams(Map<String, ?> params) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        if (MapUtil.isNotEmpty(params)) {
            params.forEach((k, v) -> {
                if (v instanceof Iterable<?> iterable) {
                    iterable.forEach(val -> map.add(k, String.valueOf(val)));
                } else {
                    map.add(k, String.valueOf(v));
                }
            });
        }
        return map;
    }

    /**
     * 构建表单参数.
     *
     * @param form 表单参数
     * @return MultiValueMap<String, Object>
     */
    protected MultiValueMap<String, Object> buildForm(Map<String, ?> form) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        if (MapUtil.isNotEmpty(form)) {
            form.forEach((k, v) -> map.add(k, String.valueOf(v)));
        }
        return map;
    }

    /**
     * 构建表单参数.
     *
     * @param form  表单参数
     * @param files 文件参数
     * @return MultiValueMap<String, Object>
     */
    protected MultiValueMap<String, Object> buildMultipart(
        Map<String, ?> form, List<MultipartFile> files) {

        MultiValueMap<String, Object> map = this.buildForm(form);

        if (CollUtil.isNotEmpty(files)) {
            for (MultipartFile file : files) {
                map.add(FileConstant.FILE, file);
            }
        }
        return map;
    }

    /**
     * 同步真实执行 HTTP 请求（子类实现）.
     *
     * @param req 请求参数
     * @return HttpExecuteResultDTO
     * @throws Exception
     */
    protected abstract HttpExecuteResultDTO doExecute(HttpRequestSpecDTO req) throws Exception;

    /**
     * 异步真实执行 HTTP 请求（子类实现）.
     *
     * @param req 请求参数
     * @return HttpExecuteResultDTO
     * @throws Exception
     */
    protected abstract CompletionStage<HttpExecuteResultDTO> doExecuteAsync(HttpRequestSpecDTO req) throws Exception;

    private HttpExecuteResultDTO executeInternal(HttpRequestSpecDTO request) {
        return executeTemplate.execute(
            getClientType().getDesc(),
            request.getMethod(),
            request.getUrl(),
            () -> {
                try {
                    return doExecute(request);
                } catch (Exception ex) {
                    return HttpExecuteResultDTO.error(ex);
                }
            }
        );
    }

    private CompletionStage<HttpExecuteResultDTO> executeAsyncInternal(HttpRequestSpecDTO request) {
        return executeTemplate
            .executeAsync(
                getClientType().getDesc(),
                request.getMethod(),
                request.getUrl(),
                () -> {
                    try {
                        return doExecuteAsync(request);
                    } catch (Exception ex) {
                        final HttpExecuteResultDTO result = HttpExecuteResultDTO.error(ex);
                        return CompletableFuture.completedFuture(result);
                    }
                }
            );
    }
}
