/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.support.observation;

import com.iwindplus.base.domain.constant.CommonConstant.ObservationConstant;
import io.micrometer.observation.Observation;

/**
 * HTTP Observation 上下文。
 *
 * <p>
 * 该 Context 是 {@link Observation} 与 {@link io.micrometer.observation.ObservationConvention} 之间的“语义载体”，用于在执行过程中逐步填充 HTTP 调用的关键状态。
 * </p>
 *
 * <p>
 * 设计原则：
 * <ul>
 *     <li>仅存放“事实数据”，不做任何 metric / tag 决策</li>
 *     <li>字段语义稳定，可被 Feign / Gateway / HttpClient 复用</li>
 *     <li>所有标签最终由 ObservationConvention 决定</li>
 * </ul>
 * </p>
 *
 * @author zengdegui
 * @since 2026/01/18
 */
public final class HttpClientObservationContext extends Observation.Context {

    /**
     * 客户端名称（如：order-service、user-service）
     */
    private final String client;

    /**
     * HTTP 方法（GET / POST / PUT / DELETE）
     */
    private final String method;

    /**
     * 原始请求 URL（高基数，仅用于排障）
     */
    private final String url;

    /**
     * HTTP 响应状态码
     */
    private Integer status;

    public HttpClientObservationContext(String client, String method, String url) {
        this.client = client;
        this.method = method;
        this.url = url;
    }

    public String getClient() {
        return client;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Integer getStatus() {
        return status;
    }

    /**
     * 设置 HTTP 状态码。
     */
    public void setStatus(Integer status) {
        this.status = status;
        put(ObservationConstant.HTTP_STATUS, status);
    }

    @Override
    public void setError(Throwable error) {
        super.setError(error);
        put(ObservationConstant.EXCEPTION, error);
    }
}
