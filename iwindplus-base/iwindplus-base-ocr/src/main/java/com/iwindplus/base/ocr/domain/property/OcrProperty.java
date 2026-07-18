/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.ocr.domain.property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * ocr相关属性.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "ocr")
public class OcrProperty {
    /**
     * 印刷文字ocr配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private PrintWordConfig printWord = new PrintWordConfig();

    /**
     * 翔云ocr配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private XiangyunConfig xiangyun = new XiangyunConfig();

    /**
     * 印刷文字ocr相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrintWordConfig {
        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;

        /**
         * 认证code.
         */
        private String appCode;
    }

    /**
     * 翔云ocr相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class XiangyunConfig {
        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;

        /**
         * 访问key.
         */
        private String accessKey;

        /**
         * 密匙.
         */
        private String secretKey;
    }
}
