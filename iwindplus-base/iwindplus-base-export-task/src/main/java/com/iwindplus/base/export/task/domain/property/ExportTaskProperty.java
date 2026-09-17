/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.domain.property;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 导出配置.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "export-task")
public class ExportTaskProperty {

    /**
     * 是否开启.
     */
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 是否开启异常信息截取.
     */
    @Builder.Default
    private Boolean enabledExceptionCapture = true;

    /**
     * 异常信息截取长度.
     */
    @Builder.Default
    private Integer exceptionCaptureLength = 4000;

    /**
     * 定时任务分页每页条数.
     */
    @Builder.Default
    private Integer maxPageSize = 10;

    /**
     * 导出数据最大条数限制（默认：10万行）.
     */
    @Builder.Default
    private Long maxExportCount = 100000L;

    /**
     * 任务执行最大时间，超过这个时间任务将被重置
     */
    @Builder.Default
    private Long timeoutSeconds = 120L;

    /**
     * OSS上传配置（导出文件上传到OSS，解决分布式部署文件无法共享的问题）.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private OssConfig oss = new OssConfig();

    /**
     * 重试策略配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private RetryConfig retry = new RetryConfig();

    /**
     * job配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private JobConfig job = new JobConfig();

    /**
     * web接口配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private WebConfig web = new WebConfig();

    /**
     * OSS上传相关属性.
     *
     * @author zengdegui
     * @since 2026/9/13
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OssConfig {

        /**
         * 是否启用OSS上传（默认：false，不启用时使用本地文件存储）.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;

        /**
         * 配置编码（必填）.
         */
        @Schema(description = "配置编码")
        private String code;

        /**
         * 模板编码（必填）.
         */
        @Schema(description = "模板编码")
        private String tplCode;

        /**
         * 存储导出文件的URL.
         */
        @Builder.Default
        private String uploadUrl = "lb://iwindplus-integr/inner/oss/uploadFile";

        /**
         * 签名访问路径的URL.
         */
        @Builder.Default
        private String listSignUrl = "lb://iwindplus-integr/inner/oss/listSignUrl";

        /**
         * 签名过期时间（可选，单位：分钟，默认：1）.
         */
        @Builder.Default
        private Integer signTimeout = 1;

        /**
         * 相对路径前缀（可选，如：export-task/）.
         */
        private String relativePathPrefix;
    }

    /**
     * 重试策略相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetryConfig {

        /**
         * 重试频率.
         */
        @Builder.Default
        private String frequency = "1m,2m,5m,10m";

        /**
         * 是否启用无限重试.
         */
        @Builder.Default
        private Boolean enabledUnlimitedRetry = Boolean.FALSE;

        /**
         * 最大重试次数.
         */
        @Builder.Default
        private Integer maxAttempts = 10;
    }

    /**
     * job相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 定时任务单次执行最大循环次数.
         */
        @Builder.Default
        private Integer maxLoopCount = 100;
    }


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
