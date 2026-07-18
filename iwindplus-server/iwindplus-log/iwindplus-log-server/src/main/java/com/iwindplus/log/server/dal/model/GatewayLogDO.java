/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.server.dal.model;

import com.iwindplus.base.es.domain.EsDbBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 网关日志.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Schema(description = "网关日志对象")
@Document(indexName = "gateway_log")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayLogDO extends EsDbBaseDO {

    /**
     * 请求唯一标识.
     */
    @Schema(description = "请求唯一标识")
    @Field(type = FieldType.Keyword)
    private String requestId;

    /**
     * 跟踪唯一标识.
     */
    @Schema(description = "跟踪唯一标识")
    @Field(type = FieldType.Keyword)
    private String bizTraceId;

    /**
     * 访问实例.
     */
    @Schema(description = "访问实例")
    @Field(type = FieldType.Keyword)
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
    @Field(type = FieldType.Text)
    private String requestPath;

    /**
     * 请求方式.
     */
    @Schema(description = "请求方式")
    @Field(type = FieldType.Keyword)
    private String requestMethod;

    /**
     * 请求参数.
     */
    @Schema(description = "请求参数")
    @Field(type = FieldType.Text, index = false)
    private String requestParam;

    /**
     * 请求头.
     */
    @Schema(description = "请求头")
    @Field(type = FieldType.Text, index = false)
    private String requestHeaders;

    /**
     * 请求体.
     */
    @Schema(description = "请求体")
    @Field(type = FieldType.Text, index = false)
    private String requestBody;

    /**
     * 响应头.
     */
    @Schema(description = "响应头")
    @Field(type = FieldType.Text, index = false)
    private String responseHeaders;

    /**
     * 响应体.
     */
    @Schema(description = "响应体")
    @Field(type = FieldType.Text, index = false)
    private String responseBody;

    /**
     * 请求时间.
     */
    @Schema(description = "请求时间")
    @Field(type = FieldType.Long)
    private Long requestTime;

    /**
     * 响应时间.
     */
    @Schema(description = "响应时间")
    @Field(type = FieldType.Long)
    private Long responseTime;

    /**
     * 执行时间（ms）.
     */
    @Schema(description = "执行时间（ms）")
    @Field(type = FieldType.Long)
    private Long executeTime;

    /**
     * 响应状态码.
     */
    @Schema(description = "响应状态码")
    @Field(type = FieldType.Integer)
    private Integer responseStatus;

    /**
     * 响应错误编码.
     */
    @Schema(description = "响应错误编码")
    @Field(type = FieldType.Keyword)
    private String responseErrorCode;

    /**
     * 响应错误信息.
     */
    @Schema(description = "响应错误信息")
    @Field(type = FieldType.Text)
    private String responseErrorMessage;

    /**
     * 平台名称.
     */
    @Schema(description = "平台名称")
    @Field(type = FieldType.Keyword)
    private String platformName;

    /**
     * 系统名称.
     */
    @Schema(description = "系统名称")
    @Field(type = FieldType.Keyword)
    private String osName;

    /**
     * 浏览器名称.
     */
    @Schema(description = "浏览器名称")
    @Field(type = FieldType.Keyword)
    private String browserName;

    /**
     * 请求ip.
     */
    @Schema(description = "请求ip")
    @Field(type = FieldType.Keyword)
    private String ip;

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    @Field(type = FieldType.Long)
    private Long userId;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    @Field(type = FieldType.Long)
    private Long orgId;
}
