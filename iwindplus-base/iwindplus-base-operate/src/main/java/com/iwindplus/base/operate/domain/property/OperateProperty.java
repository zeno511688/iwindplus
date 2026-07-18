/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.operate.domain.property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 操作配置相关属性.
 *
 * @author zengdegui
 * @since 2024/4/6
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "operate")
public class OperateProperty {

    /**
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.TRUE;

    /**
     * 操作日志配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private OperateLogConfig log = new OperateLogConfig();

    /**
     * 操作校验配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private OperateValidConfig valid = new OperateValidConfig();

    /**
     * 操作日志相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperateLogConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 存储操作日志的URL.
         */
        @Builder.Default
        private String url = "lb://iwindplus-log/inner/operation/log/save";

        /**
         * 是否启用获取请求参数.
         */
        @Builder.Default
        private Boolean enabledRequestParam = Boolean.TRUE;

        /**
         * 是否启用获取请求体.
         */
        @Builder.Default
        private Boolean enabledRequestBody = Boolean.FALSE;

        /**
         * 是否启用返回响应结果.
         */
        @Builder.Default
        private Boolean enabledResponseBody = Boolean.FALSE;
    }

    /**
     * 操作校验相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperateValidConfig {

        /**
         * 是否启用验证.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 获取用户扩展功能校验结果的URL.
         */
        @Builder.Default
        private String url = "lb://iwindplus-mgt/inner/user/checkExtendFunctionByUserId";
    }

}
