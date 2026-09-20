/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.infrastructure.configuration.property;

import com.iwindplus.gateway.infrastructure.configuration.enums.AuthTokenModeEnum;
import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 认证配置.
 *
 * @author zengdegui
 * @since 2026/09/14 20:21
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@RefreshScope
@ConfigurationProperties(prefix = "gateway.auth")
public class AuthProperty {

    /**
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.TRUE;

    /**
     * Token模式：JWT-生成jwt令牌（默认）；OPAQUE-只生成不透明令牌id，必须走远程校验.
     */
    @Builder.Default
    private AuthTokenModeEnum tokenMode = AuthTokenModeEnum.JWT;

    /**
     * 是否启用远程token校验.
     */
    @Builder.Default
    private Boolean enabledRemoteToken = Boolean.TRUE;

    /**
     * 是否启用API权限验证.
     */
    @Builder.Default
    private Boolean enabledApiPermission = Boolean.TRUE;

    /**
     * token最大缓存数量.
     */
    @Builder.Default
    private long tokenCacheMaxSize = 1000L;

    /**
     * token缓存超时时间，单位：分钟.
     */
    @Builder.Default
    private Duration tokenCacheTimeout = Duration.ofMinutes(5L);

    /**
     * token缓存刷新时间，单位：分钟.
     */
    @Builder.Default
    private Duration tokenCacheRefresh = Duration.ofMinutes(10L);

    /**
     * 权限最大缓存数量.
     */
    @Builder.Default
    private long permissionCacheMaxSize = 1000L;

    /**
     * 权限缓存超时时间，单位：分钟.
     */
    @Builder.Default
    private Duration permissionCacheTimeout = Duration.ofMinutes(10L);

    /**
     * 权限缓存刷新时间，单位：分钟.
     */
    @Builder.Default
    private Duration permissionCacheRefresh = Duration.ofMinutes(15L);

    /**
     * 忽略API鉴权的API.
     */
    private List<String> ignoredApi;

    /**
     * 通用都有权限的API.
     */
    private List<String> generalApi;
}
