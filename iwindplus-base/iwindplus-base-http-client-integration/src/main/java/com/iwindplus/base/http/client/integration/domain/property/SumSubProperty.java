/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.domain.property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * SumSub配置属性.
 *
 * @author zengdegui
 * @since 2026/08/20
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "sum-sub")
public class SumSubProperty {

    /**
     * API密钥.
     */
    private String apiKey;

    /**
     * API密钥.
     */
    private String apiSecret;

    /**
     * 默认审核级别.
     */
    @Builder.Default
    private String defaultLevelName = "basic-kyc-level";

    /**
     * 访问令牌默认过期时间（秒）.
     */
    @Builder.Default
    private Integer defaultTokenTtl = 2592000;

    /**
     * Webhook签名密钥（可选）.
     * <p>
     * 用于验证Webhook请求的签名，确保请求来自SumSub。 如果不配置，则不进行签名验证。
     * </p>
     */
    private String webhookSecretKey;

    /**
     * web接口配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private WebConfig web = new WebConfig();

    /**
     * web接口相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 接口路径（不同的服务不同的路径）.
         */
        private String path;
    }
}
