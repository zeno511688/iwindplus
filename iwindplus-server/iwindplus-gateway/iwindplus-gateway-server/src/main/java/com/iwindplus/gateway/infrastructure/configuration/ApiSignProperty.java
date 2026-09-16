/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.infrastructure.configuration;

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
 * API签名相关属性.
 *
 * @author zengdegui
 * @since 2026/09/14 20:21
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@RefreshScope
@ConfigurationProperties(prefix = "gateway.api-sign")
public class ApiSignProperty {

    /**
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.TRUE;

    /**
     * 是否启用检查只执行一次.
     */
    @Builder.Default
    private Boolean enabledExecuteOnlyOnce = Boolean.TRUE;

    /**
     * 检查只执行一次过期时间.
     */
    @Builder.Default
    private Duration onlyCheckOnceTtl = Duration.ofMinutes(1);

    /**
     * 最大缓存数量.
     */
    @Builder.Default
    private long maxSize = 1000L;

    /**
     * 缓存超时时间，单位：分钟.
     */
    @Builder.Default
    private Duration cacheTimeout = Duration.ofMinutes(10L);

    /**
     * 缓存刷新时间，单位：分钟.
     */
    @Builder.Default
    private Duration cacheRefresh = Duration.ofMinutes(15L);

    /**
     * 忽略的API.
     */
    private List<String> ignoredApi;
}
