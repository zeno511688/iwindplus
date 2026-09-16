/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.infrastructure.configuration;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * IP黑名单配置.
 *
 * @author zengdegui
 * @since 2026/09/14 20:21
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@RefreshScope
@ConfigurationProperties(prefix = "gateway.ip-black-list")
public class IpBlackListProperty {

    /**
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.TRUE;

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
}
