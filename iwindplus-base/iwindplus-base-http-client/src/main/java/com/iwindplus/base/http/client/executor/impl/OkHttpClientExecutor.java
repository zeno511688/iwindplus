/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.executor.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.web.multipart.MultipartFile;

/**
 * OkHttpClient执行器.
 *
 * @author zengdegui
 * @since 2026/01/19 23:35
 */
@Slf4j
public class OkHttpClientExecutor extends AbstractHttpClientExecutor implements HttpClientExecutor {

    private static final MediaType JSON =
        MediaType.parse(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);

    private static final MediaType OCTET_STREAM =
        MediaType.parse(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE);

    private final OkHttpClient okHttpClient;

    public OkHttpClientExecutor(
        HttpClientProperty property,
        HttpExecuteTemplate executeTemplate,
        ResponseExtractorStrategyFactory extractorStrategyFactory,
        DtpExecutor httpClientTaskExecutor,
        OkHttpClient okHttpClient) {

        super(property, executeTemplate, extractorStrategyFactory, httpClientTaskExecutor);
        this.okHttpClient = okHttpClient;
    }

    @Override
    public HttpClientTypeEnum getClientType() {
        return HttpClientTypeEnum.OK_HTTP;
    }

    @Override
    protected HttpExecuteResultDTO doExecute(HttpRequestSpecDTO req) throws Exception {
        Request request = buildRequest(req);

        try (Response resp = okHttpClient.newCall(request).execute();
            ResponseBody body = resp.body()) {
            final int code = resp.code();
            String bodyStr = body != null ? body.string() : null;
            if (resp.isSuccessful()) {
                return HttpExecuteResultDTO.success(code, bodyStr);
            }
            return HttpExecuteResultDTO.error(code, bodyStr);
        }
    }

    @Override
    protected CompletionStage<HttpExecuteResultDTO> doExecuteAsync(HttpRequestSpecDTO req) throws Exception {
        Request request = buildRequest(req);
        Call call = okHttpClient.newCall(request);

        CompletableFuture<HttpExecuteResultDTO> future = new CompletableFuture<>();
        future.whenCompleteAsync((r, t) -> {
            if (future.isCancelled()) {
                call.cancel();
            }
        });

        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.complete(HttpExecuteResultDTO.error(e));
            }

            @Override
            public void onResponse(Call call, Response resp) {
                final int code = resp.code();

                try (ResponseBody body = resp.body()) {
                    String bodyStr = body != null ? body.string() : null;
                    if (resp.isSuccessful()) {
                        future.complete(HttpExecuteResultDTO.success(code, bodyStr));
                    } else {
                        future.complete(HttpExecuteResultDTO.error(code, bodyStr));
                    }
                } catch (Exception ex) {
                    future.complete(HttpExecuteResultDTO.error(code, ex));
                }
            }
        });

        return future;
    }

    private Request buildRequest(HttpRequestSpecDTO req) {
        HttpUrl url = buildHttpUrl(req);
        Request.Builder builder = new Request.Builder().url(url);

        if (MapUtil.isNotEmpty(req.getHeaders())) {
            req.getHeaders().forEach(builder::addHeader);
        }

        RequestBody body = buildRequestBody(req);
        builder.method(req.getMethod(), body != null ? body : Util.EMPTY_REQUEST);

        return builder.build();
    }

    private RequestBody buildRequestBody(HttpRequestSpecDTO req) {
        HttpBodyTypeEnum type = req.getBodyType();
        if (type == null || type == HttpBodyTypeEnum.NONE) {
            return null;
        }

        return switch (type) {
            case JSON -> buildJsonBody(req.getBody());
            case FORM -> buildFormBody(req.getForm());
            case MULTIPART -> buildMultipartBody(req.getForm(), req.getFiles());
            default -> throw new BizException(BizCodeEnum.UNSUPPORTED_TYPE, type.getDesc());
        };
    }

    private RequestBody buildJsonBody(Object body) {
        if (body == null) {
            return null;
        }
        byte[] bytes;
        if (body instanceof String str) {
            bytes = str.getBytes(StandardCharsets.UTF_8);
        } else {
            bytes = JacksonUtil.toJsonBytes(body);
        }
        return RequestBody.create(bytes, JSON);
    }

    private RequestBody buildFormBody(Map<String, ?> form) {
        FormBody.Builder fb = new FormBody.Builder();
        if (MapUtil.isNotEmpty(form)) {
            form.forEach((k, v) -> fb.add(k, String.valueOf(v)));
        }
        return fb.build();
    }

    private RequestBody buildMultipartBody(Map<String, ?> form, List<MultipartFile> files) {
        MultipartBody.Builder mb = new MultipartBody.Builder().setType(MultipartBody.FORM);

        if (MapUtil.isNotEmpty(form)) {
            form.forEach((k, v) -> mb.addFormDataPart(k, String.valueOf(v)));
        }

        if (CollUtil.isNotEmpty(files)) {
            for (MultipartFile f : files) {
                mb.addFormDataPart(FileConstant.FILE, f.getOriginalFilename(), createFileRequestBody(f));
            }
        }

        return mb.build();
    }

    private RequestBody createFileRequestBody(MultipartFile file) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse(Objects.toString(file.getContentType(), OCTET_STREAM.toString()));
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                try (InputStream is = file.getInputStream()) {
                    sink.writeAll(Okio.source(is));
                }
            }

            @Override
            public long contentLength() {
                return file.getSize();
            }
        };
    }

    private HttpUrl buildHttpUrl(HttpRequestSpecDTO spec) {
        HttpUrl base = HttpUrl.parse(spec.getUrl());
        if (base == null) {
            throw new IllegalArgumentException("Invalid url: " + spec.getUrl());
        }

        HttpUrl.Builder builder = base.newBuilder();
        Map<String, ?> query = spec.getQuery();
        if (MapUtil.isNotEmpty(query)) {
            query.forEach((k, v) -> {
                if (v instanceof Iterable<?> iterable) {
                    iterable.forEach(val -> builder.addQueryParameter(k, String.valueOf(val)));
                } else {
                    builder.addQueryParameter(k, String.valueOf(v));
                }
            });
        }

        return builder.build();
    }
}