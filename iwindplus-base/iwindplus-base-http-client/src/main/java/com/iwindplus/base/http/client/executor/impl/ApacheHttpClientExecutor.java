/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.executor.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.FileConstant;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Apache HttpClient 执行器
 *
 * @author zengdegui
 * @since 2026/01/19
 */
@Slf4j
public class ApacheHttpClientExecutor extends AbstractHttpClientExecutor implements HttpClientExecutor {

    private final CloseableHttpClient closeableHttpClient;
    private final CloseableHttpAsyncClient closeableHttpAsyncClient;

    public ApacheHttpClientExecutor(
        HttpClientProperty property,
        HttpExecuteTemplate executeTemplate,
        ResponseExtractorStrategyFactory extractorStrategyFactory,
        DtpExecutor httpClientTaskExecutor,
        CloseableHttpClient closeableHttpClient,
        CloseableHttpAsyncClient closeableHttpAsyncClient) {

        super(property, executeTemplate, extractorStrategyFactory, httpClientTaskExecutor);
        this.closeableHttpClient = closeableHttpClient;
        this.closeableHttpAsyncClient = closeableHttpAsyncClient;
    }

    @Override
    public HttpClientTypeEnum getClientType() {
        return HttpClientTypeEnum.HTTP_CLIENT;
    }

    @Override
    protected HttpExecuteResultDTO doExecute(HttpRequestSpecDTO req) throws Exception {
        HttpUriRequestBase request = new HttpUriRequestBase(
            req.getMethod(), buildUrl(req.getUrl(), req.getQuery())
        );

        if (MapUtil.isNotEmpty(req.getHeaders())) {
            req.getHeaders().forEach(request::addHeader);
        }

        BodySpec bodySpec = buildBody(req);
        if (bodySpec != null && bodySpec.entity != null) {
            request.setEntity(bodySpec.entity);
        }

        return closeableHttpClient.execute(request, response -> {
            HttpEntity entity = response.getEntity();
            String body = entity == null ? null : EntityUtils.toString(entity, StandardCharsets.UTF_8);
            final int code = response.getCode();
            if (code >= 200 && code < 300) {
                return HttpExecuteResultDTO.success(code, body);
            }
            return HttpExecuteResultDTO.error(code, body);
        });
    }

    @Override
    protected CompletionStage<HttpExecuteResultDTO> doExecuteAsync(HttpRequestSpecDTO req) throws Exception {
        SimpleHttpRequest request = SimpleHttpRequest.create(
            req.getMethod(), buildUrl(req.getUrl(), req.getQuery())
        );

        if (MapUtil.isNotEmpty(req.getHeaders())) {
            req.getHeaders().forEach(request::addHeader);
        }

        BodySpec bodySpec = buildBody(req);
        if (bodySpec != null && bodySpec.bytes != null) {
            request.setBody(bodySpec.bytes, bodySpec.contentType);
        }

        CompletableFuture<HttpExecuteResultDTO> future = new CompletableFuture<>();
        closeableHttpAsyncClient.execute(request, new FutureCallback<>() {
            @Override
            public void completed(SimpleHttpResponse response) {
                final int code = response.getCode();
                if (code >= 200 && code < 300) {
                    future.complete(HttpExecuteResultDTO.success(code, response.getBodyText()));
                } else {
                    future.complete(HttpExecuteResultDTO.error(code, response.getBodyText()));
                }
            }

            @Override
            public void failed(Exception ex) {
                future.complete(HttpExecuteResultDTO.error(ex));
            }

            @Override
            public void cancelled() {
                future.cancel(true);
            }
        });

        return future;
    }

    private URI buildUrl(String url, Map<String, ?> param) {
        try {
            if (MapUtil.isEmpty(param)) {
                return URI.create(url);
            }
            URIBuilder builder = new URIBuilder(url);
            param.forEach((k, v) -> {
                if (v instanceof Iterable<?> iterable) {
                    iterable.forEach(val -> builder.addParameter(k, String.valueOf(val)));
                } else {
                    builder.addParameter(k, String.valueOf(v));
                }
            });
            return builder.build();
        } catch (URISyntaxException ex) {
            log.error(ExceptionConstant.URI_SYNTAX_EXCEPTION, ex);
            throw new IllegalArgumentException("Invalid url: " + url);
        }
    }

    private BodySpec buildBody(HttpRequestSpecDTO req) {
        HttpBodyTypeEnum bodyType = req.getBodyType();
        if (bodyType == null || bodyType == HttpBodyTypeEnum.NONE) {
            return null;
        }

        return switch (bodyType) {
            case JSON -> buildJsonBody(req);
            case FORM -> buildFormBody(req);
            case MULTIPART -> buildMultipartBody(req);
            default -> throw new BizException(BizCodeEnum.UNSUPPORTED_TYPE, bodyType.getDesc());
        };
    }

    private BodySpec buildJsonBody(HttpRequestSpecDTO req) {
        Object body = req.getBody();
        if (body == null) {
            return null;
        }
        byte[] bytes;
        if (body instanceof String str) {
            bytes = str.getBytes(StandardCharsets.UTF_8);
        } else {
            bytes = JacksonUtil.toJsonBytes(body);
        }
        HttpEntity entity = EntityBuilder.create().setBinary(bytes)
            .setContentType(ContentType.APPLICATION_JSON).build();
        return new BodySpec(bytes, ContentType.APPLICATION_JSON, entity);
    }

    private BodySpec buildFormBody(HttpRequestSpecDTO req) {
        Map<String, ?> form = req.getForm();
        if (MapUtil.isEmpty(form)) {
            return null;
        }

        List<NameValuePair> params = new ArrayList<>(form.size());
        form.forEach((k, v) -> params.add(new BasicNameValuePair(k, String.valueOf(v))));

        HttpEntity entity = EntityBuilder.create()
            .setParameters(params)
            .setContentType(ContentType.APPLICATION_FORM_URLENCODED)
            .build();
        return new BodySpec(null, ContentType.APPLICATION_FORM_URLENCODED, entity);
    }

    private BodySpec buildMultipartBody(HttpRequestSpecDTO req) {
        MultipartEntityBuilder mb = MultipartEntityBuilder.create();

        Map<String, ?> form = req.getForm();
        if (MapUtil.isNotEmpty(form)) {
            form.forEach((k, v) -> mb.addTextBody(k, String.valueOf(v), ContentType.TEXT_PLAIN));
        }

        List<MultipartFile> files = req.getFiles();
        if (CollUtil.isNotEmpty(files)) {
            for (MultipartFile f : files) {
                try {
                    mb.addBinaryBody(
                        FileConstant.FILE,
                        f.getBytes(),
                        ContentType.parse(Objects.toString(f.getContentType(), ContentType.APPLICATION_OCTET_STREAM.getMimeType())),
                        f.getOriginalFilename()
                    );
                } catch (Exception ex) {
                    log.error(ExceptionConstant.IO_EXCEPTION, ex);
                }
            }
        }

        return new BodySpec(null, ContentType.MULTIPART_FORM_DATA, mb.build());
    }

    /**
     * Body 构建结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static final class BodySpec {

        private byte[] bytes;
        private ContentType contentType;
        private HttpEntity entity;
    }
}