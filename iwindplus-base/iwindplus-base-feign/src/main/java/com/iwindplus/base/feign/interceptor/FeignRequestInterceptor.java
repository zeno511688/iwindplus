/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.feign.interceptor;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.context.HeaderContextHolder;
import com.iwindplus.base.feign.domain.property.FeignProperty;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.annotation.Resource;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Feign请求过滤器.
 *
 * @author zengdegui
 * @since 2020/4/23
 */
@Slf4j
public class FeignRequestInterceptor implements RequestInterceptor {

    @Resource
    private FeignProperty property;

    @Resource
    private TraceContextPropagator traceContextPropagator;

    @Override
    public void apply(RequestTemplate template) {
        if (Boolean.FALSE.equals(property.getRequest().getEnabled())) {
            return;
        }

        traceContextPropagator.inject(
            template,
            (carrier, key, value) -> carrier.header(key, value)
        );

        propagateHeaders(template);
    }

    private void propagateHeaders(RequestTemplate template) {
        Map<String, String> headers = HeaderContextHolder.getContext();

        if (MapUtil.isEmpty(headers)) {
            return;
        }

        headers.forEach((key, value) -> setHeaderIfAbsent(template, key, value));
    }

    private void setHeaderIfAbsent(RequestTemplate template, String key, String value) {
        if (CharSequenceUtil.isBlank(key) || CharSequenceUtil.isBlank(value)) {
            return;
        }

        if (template.headers().containsKey(key)) {
            return;
        }

        template.header(key, value);
    }
}