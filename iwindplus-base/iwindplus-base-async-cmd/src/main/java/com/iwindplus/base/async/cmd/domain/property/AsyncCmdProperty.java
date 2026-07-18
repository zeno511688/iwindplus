/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 异步命令配置.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "async-cmd")
public class AsyncCmdProperty {

    /**
     * 是否开启.
     */
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 是否开启成功后删除数据.
     */
    @Builder.Default
    private Boolean enabledSuccessDelete = true;

    /**
     * 是否开启成功后真实删除数据.
     */
    @Builder.Default
    private Boolean enabledSuccessRealDelete = true;

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
     * 任务执行最大时间，超过这个时间任务将被重置
     */
    @Builder.Default
    private Long timeoutSeconds = 60L;

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
        private String frequency = "30s,2m,10m,15m,20m,30m,1h";

        /**
         * 是否启用无限重试.
         */
        @Builder.Default
        private Boolean enabledUnlimitedRetry = Boolean.FALSE;

        /**
         * 最大重试次数.
         */
        @Builder.Default
        private Integer maxAttempts = 15;
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
    }
}
