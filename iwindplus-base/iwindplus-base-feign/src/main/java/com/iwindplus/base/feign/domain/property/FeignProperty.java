/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.feign.domain.property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * feign 相关属性.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "feign")
public class FeignProperty {

    /**
     * feign 请求配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private FeignRequestConfig request = new FeignRequestConfig();

    /**
     * feign 统一异常解码配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private FeignErrorConfig error = new FeignErrorConfig();

    /**
     * feign 默认回退配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private FeignFallbackConfig fallback = new FeignFallbackConfig();

    /**
     * feign 请求相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeignRequestConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;
    }

    /**
     * feign 统一异常解码相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeignErrorConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;
    }

    /**
     * feign 默认回退配置.
     *
     * @author zengdegui
     * @since 2026/8/25
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeignFallbackConfig {

        /**
         * 是否为未声明 fallback 的 Feign 客户端启用默认回退.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;
    }

}
