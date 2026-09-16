/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.infrastructure.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 基础过滤器配置.
 *
 * @author zengdegui
 * @since 2026/09/14 20:21
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@RefreshScope
@ConfigurationProperties(prefix = "gateway.base")
public class BaseProperty {

    /**
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.TRUE;

    /**
     * 是否启用从cookie中读取token.
     */
    @Builder.Default
    private Boolean enabledAuthCookie = Boolean.FALSE;

    /**
     * 是否启用相对路径.
     */
    @Builder.Default
    private Boolean enabledRelativePath = Boolean.FALSE;
}
