/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.ocr.domain.property;

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
 * OCR相关属性.
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
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.TRUE;

    /**
     * 默认配置编码（可选）.
     */
    private String defaultCode;

    /**
     * 印刷文字OCR配置列表.
     */
    @Builder.Default
    private List<PrintWordConfig> printWord = new ArrayList<>(10);

    /**
     * 翔云OCR配置列表.
     */
    @Builder.Default
    private List<XiangyunConfig> xiangyun = new ArrayList<>(10);

    /**
     * OCR基础配置.
     *
     * @author zengdegui
     * @since 2026/9/4
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BaseConfig {

        /**
         * 配置编码（唯一标识）.
         */
        private String code;

        /**
         * 配置名称.
         */
        private String name;

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;

        /**
         * 优先级（数字越小优先级越高，用于自动故障转移）.
         */
        private Integer priority;
    }

    /**
     * 印刷文字OCR相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrintWordConfig extends BaseConfig {

        /**
         * 认证code.
         */
        private String appCode;
    }

    /**
     * 翔云OCR相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class XiangyunConfig extends BaseConfig {

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
