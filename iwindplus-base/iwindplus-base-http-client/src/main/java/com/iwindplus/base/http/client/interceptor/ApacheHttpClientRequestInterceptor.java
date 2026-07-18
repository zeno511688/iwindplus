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
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;

/**
 * ApacheHttpClient请求拦截器.
 *
 * @author zengdegui
 * @since 2020/4/23
 */
@Slf4j
public record ApacheHttpClientRequestInterceptor(
    HttpClientProperty property,
    TraceContextPropagator traceContextPropagator,
    CircuitBreakerRegistry circuitBreakerRegistry,
    ApiProtectionProvider apiProtectionProvider) implements HttpRequestInterceptor {

    @Override
    public void process(HttpRequest httpRequest, EntityDetails entityDetails, HttpContext httpContext) throws HttpException, IOException {
        injectTrace(httpRequest);
        injectTcc(httpRequest);
        injectApiSign(httpRequest);
    }

    private void injectTrace(HttpRequest httpRequest) {
        traceContextPropagator.inject(
            httpRequest,
            (carrier, key, value) -> carrier.setHeader(key, value)
        );
    }

    private void injectTcc(HttpRequest httpRequest) {
        String xid = TccContextHolder.getXid();

        if (CharSequenceUtil.isBlank(xid)) {
            return;
        }

        httpRequest.setHeader(HeaderConstant.X_TCC_XID, xid);
    }

    private void injectApiSign(HttpRequest httpRequest) {
        final String path = httpRequest.getPath().split("\\?")[0];
        if (CharSequenceUtil.isBlank(path)) {
            return;
        }

        // 加载签名配置
        final ApiSignGenerateDTO entity = this.apiProtectionProvider.buildSignGenerate(path, httpRequest.getMethod());
        if (entity == null) {
            return;
        }

        String sign = ApiSignUtil.generateSign(entity);

        httpRequest.setHeader(ApiSignConstant.X_TIMESTAMP, entity.getTimestamp());
        httpRequest.setHeader(ApiSignConstant.X_NONCE, entity.getNonce());
        httpRequest.setHeader(ApiSignConstant.X_PATH, entity.getPath());
        httpRequest.setHeader(ApiSignConstant.X_METHOD, entity.getMethod());
        httpRequest.setHeader(ApiSignConstant.X_SIGN, sign);
        httpRequest.setHeader(ApiSignConstant.APPLICATION, entity.getApplication());
    }
}
