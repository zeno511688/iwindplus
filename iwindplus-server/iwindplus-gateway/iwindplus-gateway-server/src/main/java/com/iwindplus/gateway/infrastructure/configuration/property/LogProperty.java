/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.infrastructure.configuration.property;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 日志配置.
 *
 * @author zengdegui
 * @since 2026/09/14 20:21
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@RefreshScope
@ConfigurationProperties(prefix = "gateway.log")
public class LogProperty {

    /**
     * 是否启用记录日志（用户登陆后）.
     */
    @Builder.Default
    private Boolean enabled = Boolean.FALSE;

    /**
     * 是否启用记录错误日志.
     */
    @Builder.Default
    private Boolean enabledError = Boolean.FALSE;

    /**
     * 忽略记录日志的API.
     */
    private List<String> ignoredApi;

    /**
     * 采样率（10：代表10%的采样率）.
     */
    @Builder.Default
    private Integer sampleRate = 100;

    /**
     * 是否启用获取请求头.
     */
    @Builder.Default
    private Boolean enabledRequestHeader = Boolean.TRUE;

    /**
     * 是否启用获取请求参数.
     */
    @Builder.Default
    private Boolean enabledRequestParam = Boolean.TRUE;

    /**
     * 是否启用获取请求体.
     */
    @Builder.Default
    private Boolean enabledRequestBody = Boolean.TRUE;

    /**
     * 请求体限制大小，超过部分丢弃，单位：KB.
     */
    @Builder.Default
    private Integer limitRequestBody = 16;

    /**
     * 是否启用获取响应头.
     */
    @Builder.Default
    private Boolean enabledResponseHeader = Boolean.FALSE;

    /**
     * 是否启用获取响应体.
     */
    @Builder.Default
    private Boolean enabledResponseBody = Boolean.FALSE;

    /**
     * 响应体限制大小，超过部分丢弃，单位：KB.
     */
    @Builder.Default
    private Integer limitResponseBody = 16;
}
