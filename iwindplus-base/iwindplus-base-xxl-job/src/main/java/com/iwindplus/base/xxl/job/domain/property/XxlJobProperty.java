/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.xxl.job.domain.property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * job相关属性.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "xxl-job")
public class XxlJobProperty {

    /**
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.TRUE;

    /**
     * 管理配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private AdminConfig admin = new AdminConfig();

    /**
     * 执行器配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private ExecutorConfig executor = new ExecutorConfig();

    /**
     * 管理相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminConfig {

        /**
         * 调度中心地址（必填）.
         */
        private String addresses;

        /**
         * 访问token（可选）.
         */
        private String accessToken;
    }

    /**
     * 执行器相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutorConfig {

        /**
         * 注册地址（可选）.
         */
        private String addresses;

        /**
         * 应用名称（可选）.
         */
        private String appName;

        /**
         * ip（可选）.
         */
        private String ip;

        /**
         * 端口（可选）.
         */
        private int port;

        /**
         * 日志（可选）.
         */
        private String logPath;

        /**
         * 日志保留天数（可选）.
         */
        private int logRetentionDays;
    }
}
