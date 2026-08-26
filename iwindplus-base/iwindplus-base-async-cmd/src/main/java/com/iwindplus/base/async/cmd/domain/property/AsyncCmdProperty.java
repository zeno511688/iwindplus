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
    private Boolean enabledSuccessDelete = false;

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
    private Long timeoutSeconds = 120L;

    /**
     * 等待异步结果的时间间隔，隔多久轮询一次回调结果
     */
    @Builder.Default
    private Long asyncWaitPollSeconds = 60L;

    /**
     * 等待异步结果的超时时间，从首次挂起算起，超过了整组转失败进重试链
     */
    @Builder.Default
    private Long asyncWaitTimeoutSeconds = 1800L;

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
        private Integer maxAttempts = 30;
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
