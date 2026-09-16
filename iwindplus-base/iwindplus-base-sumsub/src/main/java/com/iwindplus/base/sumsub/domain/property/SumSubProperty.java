/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sumsub.domain.property;

import com.iwindplus.base.domain.dto.AkSkDTO;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * SumSub服务相关属性.
 *
 * @author zengdegui
 * @since 2026/9/7
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "sumsub")
public class SumSubProperty {

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
     * SumSub配置列表.
     */
    @Builder.Default
    private List<SumSubConfig> configs = new ArrayList<>(10);

    /**
     * SumSub基础配置.
     *
     * @author zengdegui
     * @since 2026/9/7
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BaseConfig extends AkSkDTO {

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
         * 优先级（数字越小优先级越高，用于自动故障转移）.
         */
        private Integer priority;

        /**
         * Webhook签名密钥（可选）.
         * <p>
         * 用于验证Webhook请求的签名，确保请求来自服务商。
         * 如果不配置，则不进行签名验证。
         * </p>
         */
        private String webhookSecretKey;

        /**
         * 默认审核级别.
         */
        @Builder.Default
        private String defaultLevelName = "basic-kyc-level";

        /**
         * 访问令牌默认过期时间（秒）.
         */
        @Builder.Default
        private Integer defaultTokenTtl = 60;
    }

    /**
     * SumSub配置.
     *
     * @author zengdegui
     * @since 2026/9/7
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SumSubConfig extends BaseConfig {

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
     * @since 2026/9/7
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
