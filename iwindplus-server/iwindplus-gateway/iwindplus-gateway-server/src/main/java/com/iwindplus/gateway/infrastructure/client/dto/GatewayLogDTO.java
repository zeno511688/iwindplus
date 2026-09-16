/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.infrastructure.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 网关日志.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Schema(description = "网关日志数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayLogDTO implements Serializable {

    /**
     * 创建人.
     */
    @Schema(description = "创建人")
    private String createdBy;

    /**
     * 创建人主键.
     */
    @Schema(description = "创建人主键")
    private Long createdId;

    /**
     * 更新人.
     */
    @Schema(description = "更新人")
    private String modifiedBy;

    /**
     * 更新人主键.
     */
    @Schema(description = "更新人主键")
    private Long modifiedId;

    /**
     * 请求唯一标识.
     */
    @Schema(description = "请求唯一标识")
    private String requestId;

    /**
     * 跟踪唯一标识.
     */
    @Schema(description = "跟踪唯一标识")
    private String bizTraceId;

    /**
     * 访问实例.
     */
    @Schema(description = "访问实例")
    private String targetServer;

    /**
     * 请求协议.
     */
    @Schema(description = "请求协议")
    private String requestSchema;

    /**
     * 请求路径.
     */
    @Schema(description = "请求路径")
    private String requestPath;

    /**
     * 请求方式.
     */
    @Schema(description = "请求方式")
    private String requestMethod;

    /**
     * 请求参数.
     */
    @Schema(description = "请求参数")
    private String requestParam;

    /**
     * 请求头.
     */
    @Schema(description = "请求头")
    private String requestHeaders;

    /**
     * 请求体.
     */
    @Schema(description = "请求体")
    private String requestBody;

    /**
     * 响应头.
     */
    @Schema(description = "响应头")
    private String responseHeaders;

    /**
     * 响应体.
     */
    @Schema(description = "响应体")
    private String responseBody;

    /**
     * 请求时间.
     */
    @Schema(description = "请求时间")
    private String requestTime;

    /**
     * 响应时间.
     */
    @Schema(description = "响应时间")
    private String responseTime;

    /**
     * 执行时间（ms）.
     */
    @Schema(description = "执行时间（ms）")
    private Long executeTime;

    /**
     * 响应状态码.
     */
    @Schema(description = "响应状态码")
    private Integer responseStatus;

    /**
     * 响应错误编码.
     */
    @Schema(description = "响应错误编码")
    private String responseErrorCode;

    /**
     * 响应错误信息.
     */
    @Schema(description = "响应错误信息")
    private String responseErrorMessage;

    /**
     * 平台名称.
     */
    @Schema(description = "平台名称")
    private String platformName;

    /**
     * 系统名称.
     */
    @Schema(description = "系统名称")
    private String osName;

    /**
     * 系统版本.
     */
    @Schema(description = "系统版本")
    private String osVersion;

    /**
     * 浏览器名称.
     */
    @Schema(description = "浏览器名称")
    private String browserName;

    /**
     * 浏览器版本.
     */
    @Schema(description = "浏览器版本")
    private String browserVersion;

    /**
     * 设备号.
     */
    @Schema(description = "设备号")
    private String deviceNumber;

    /**
     * 设备版本.
     */
    @Schema(description = "设备版本")
    private String deviceVersion;

    /**
     * 设备指纹.
     */
    @Schema(description = "设备指纹")
    private String deviceFingerprint;

    /**
     * 请求ip.
     */
    @Schema(description = "请求ip")
    private String ip;

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    private Long userId;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}
