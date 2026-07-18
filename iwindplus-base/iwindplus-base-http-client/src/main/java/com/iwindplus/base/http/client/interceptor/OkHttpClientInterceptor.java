/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.interceptor;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant.ApiSignConstant;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.context.TccContextHolder;
import com.iwindplus.base.http.client.domain.property.HttpClientProperty;
import com.iwindplus.base.http.client.support.ApiProtectionProvider;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import com.iwindplus.base.util.ApiSignUtil;
import com.iwindplus.base.util.domain.dto.ApiSignGenerateDTO;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

/**
 * OkHttpClient拦截器.
 *
 * @author zengdegui
 * @since 2025/12/19 21:30
 */
@Slf4j
public record OkHttpClientInterceptor(
    HttpClientProperty property,
    TraceContextPropagator traceContextPropagator,
    CircuitBreakerRegistry circuitBreakerRegistry,
    ApiProtectionProvider apiProtectionProvider) implements Interceptor {

    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = enhanceRequest(chain.request());
        return chain.proceed(request);
    }

    private Request enhanceRequest(Request request) {
        Request.Builder builder = request.newBuilder();
        injectTrace(builder);
        injectTcc(builder);
        Request enhanced = builder.build();
        return injectApiSign(enhanced);
    }

    private void injectTrace(Request.Builder builder) {
        traceContextPropagator.inject(
            builder,
            (carrier, key, value) -> carrier.header(key, value)
        );
    }

    private void injectTcc(Request.Builder builder) {
        String xid = TccContextHolder.getXid();

        if (CharSequenceUtil.isBlank(xid)) {
            return;
        }

        builder.header(HeaderConstant.X_TCC_XID, xid);
    }

    private Request injectApiSign(Request request) {
        final String path = request.url().url().getPath().split("\\?")[0];
        if (CharSequenceUtil.isBlank(path)) {
            return request;
        }

        ApiSignGenerateDTO entity = apiProtectionProvider.buildSignGenerate(path, request.method());
        if (entity == null) {
            return request;
        }

        String sign = ApiSignUtil.generateSign(entity);

        return request.newBuilder()
            .header(ApiSignConstant.X_TIMESTAMP, entity.getTimestamp())
            .header(ApiSignConstant.X_NONCE, entity.getNonce())
            .header(ApiSignConstant.X_PATH, entity.getPath())
            .header(ApiSignConstant.X_METHOD, entity.getMethod())
            .header(ApiSignConstant.X_SIGN, sign)
            .header(ApiSignConstant.APPLICATION, entity.getApplication())
            .build();
    }
}
