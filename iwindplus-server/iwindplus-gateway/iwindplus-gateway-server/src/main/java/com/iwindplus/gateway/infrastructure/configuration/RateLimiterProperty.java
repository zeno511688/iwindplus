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
import org.redisson.api.RateType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 限流过滤器配置.
 *
 * @author zengdegui
 * @since 2026/09/14 20:21
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@RefreshScope
@ConfigurationProperties(prefix = "gateway.rate-limiter")
public class RateLimiterProperty {

    /**
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.TRUE;

    /**
     * 是否按路径限流（可选，默认：true）.
     *
     * @return boolean
     */
    @Builder.Default
    private Boolean enabledLimitPath = Boolean.FALSE;

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
     * 限流类型（可选，默认：OVERALL）.
     */
    @Builder.Default
    private RateType rateType = RateType.OVERALL;

    /**
     * 限流次数，每个时间窗口允许请求数量（可选，默认：2000）.
     */
    @Builder.Default
    private Long rate = 2000L;

    /**
     * 限流速率（可选，默认：1s）.
     */
    @Builder.Default
    private Duration rateInterval = Duration.ofSeconds(1);
}
