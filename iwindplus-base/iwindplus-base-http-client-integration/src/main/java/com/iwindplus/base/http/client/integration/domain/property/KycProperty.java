/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.domain.property;

import com.iwindplus.base.http.client.integration.domain.enums.KycProviderEnum;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * KYC服务配置属性.
 *
 * @author zengdegui
 * @since 2026/08/21
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "kyc")
public class KycProperty {

    /**
     * 是否启用KYC服务.
     */
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 提供商配置映射（key: 提供商编码）.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private Map<String, ProviderConfig> providers = new HashMap<>(16);

    /**
     * 提供商配置.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = true;

        /**
         * 优先级（数字越小优先级越高）.
         */
        private Integer priority;

        /**
         * API密钥.
         */
        private String apiKey;

        /**
         * API密钥.
         */
        private String apiSecret;

        /**
         * Webhook签名密钥（可选）.
         * <p>
         * 用于验证Webhook请求的签名，确保请求来自服务商。
         * 如果不配置，则不进行签名验证。
         * </p>
         */
        private String webhookSecretKey;

        /**
         * 默认审核级别（SumSub专用）.
         */
        private String defaultLevelName;

        /**
         * 访问令牌默认过期时间（秒）（SumSub专用）.
         */
        private Integer defaultTokenTtl;

        /**
         * Web接口配置.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private WebConfig web = new WebConfig();
    }

    /**
     * Web接口相关属性.
     *
     * @author zengdegui
     * @since 2026/08/21
     */
    @Data
    @Builder
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

    /**
     * 根据提供商编码获取配置.
     *
     * @param code 提供商编码
     * @return 配置
     */
    public ProviderConfig getProviderConfig(String code) {
        return this.providers.get(code);
    }

    /**
     * 根据提供商枚举获取配置.
     *
     * @param provider 提供商枚举
     * @return 配置
     */
    public ProviderConfig getProviderConfig(KycProviderEnum provider) {
        return getProviderConfig(provider.getCode());
    }

    /**
     * 获取启用的提供商配置映射（按优先级排序）.
     *
     * @return 配置映射（key: 提供商编码, value: 配置）
     */
    public Map<String, ProviderConfig> getEnabledProviders() {
        return this.providers.entrySet().stream()
                .filter(entry -> Boolean.TRUE.equals(entry.getValue().getEnabled()))
                .sorted((a, b) -> {
                    int priorityA = a.getValue().getPriority() != null ? a.getValue().getPriority() : Integer.MAX_VALUE;
                    int priorityB = b.getValue().getPriority() != null ? b.getValue().getPriority() : Integer.MAX_VALUE;
                    return Integer.compare(priorityA, priorityB);
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
    }
}
