/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 应用API视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "应用API视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AppApiVO implements Serializable {

    /**
     * 应用名称.
     */
    @Schema(description = "应用名称")
    private String appName;

    /**
     * 应用备注.
     */
    @Schema(description = "应用备注")
    private String appRemark;

    /**
     * API列表.
     */
    @Schema(description = "API列表")
    private List<ApiInfoVO> apis;

    /**
     * API信息视图对象.
     *
     * @author zengdegui
     * @since 2020/4/24
     */
    @Schema(description = "API信息视图对象")
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiInfoVO implements Serializable {

        /**
         * 控制器名称.
         */
        @Schema(description = "控制器名称")
        private String controllerName;

        /**
         * 类名.
         */
        @Schema(description = "类名")
        private String className;

        /**
         * 方法名.
         */
        @Schema(description = "方法名")
        private String methodName;

        /**
         * API名称.
         */
        @Schema(description = "API名称")
        private String apiName;

        /**
         * 请求方式.
         */
        @Schema(description = "请求方式")
        private String requestMethod;

        /**
         * API路径.
         */
        @Schema(description = "API路径")
        private String apiUrl;

        /**
         * 是否隐藏.
         */
        @Schema(description = "是否隐藏")
        private Boolean hideFlag;
    }
}
