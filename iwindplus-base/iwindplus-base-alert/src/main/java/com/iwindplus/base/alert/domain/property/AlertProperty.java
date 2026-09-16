/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.alert.domain.property;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 告警相关属性.
 *
 * @author zengdegui
 * @since 2025/11/23 21:18
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "alert")
public class AlertProperty {

    /**
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.TRUE;

    /**
     * 默认配置编码（可选）.
     */
    private String defaultCode;

    /**
     * 飞书配置列表.
     */
    @Builder.Default
    private List<FeishuConfig> feishu = new ArrayList<>(10);

    /**
     * 基础配置.
     *
     * @author zengdegui
     * @since 2026/9/9
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public abstract static class BaseConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 配置编码（唯一标识）.
         */
        private String code;

        /**
         * 配置名称.
         */
        private String name;

        /**
         * 优先级（数字越小优先级越高）.
         */
        private Integer priority;
    }

    /**
     * 飞书配置配置.
     *
     * @author zengdegui
     * @since 2026/9/9
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeishuConfig extends BaseConfig {

        /**
         * 企业应用消息配置集合.
         */
        @Builder.Default
        private List<AppConfig> apps = new ArrayList<>(10);

        /**
         * Webhook消息配置集合.
         */
        @Builder.Default
        private List<WebhookConfig> webhooks = new ArrayList<>(10);
    }

    /**
     * 企业应用消息配置.
     *
     * @author zengdegui
     * @since 2026/9/9
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppConfig extends BaseConfig {

        /**
         * 应用主键.
         */
        private String appId;

        /**
         * 应用密钥.
         */
        private String appSecret;
    }

    /**
     * Webhook消息配置.
     *
     * @author zengdegui
     * @since 2026/9/9
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebhookConfig extends BaseConfig {

        /**
         * Webhook地址.
         */
        private String url;

        /**
         * 密钥（可选）.
         */
        private String secretKey;
    }
}
