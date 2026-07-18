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
 * 操作日志.
 *
 * @author zengdegui
 * @since 2024/4/10
 */
@Schema(description = "操作日志对象")
@Document(indexName = "operation_log")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogDO extends EsDbBaseDO {

    /**
     * 请求唯一标识.
     */
    @Schema(description = "请求唯一标识")
    @Field(type = FieldType.Keyword)
    private String requestId;

    /**
     * 访问实例.
     */
    @Schema(description = "访问实例")
    @Field(type = FieldType.Keyword)
    private String targetServer;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    @Field(type = FieldType.Text, index = false)
    private String bizNumber;

    /**
     * 业务类型.
     */
    @Schema(description = "业务类型")
    @Field(type = FieldType.Text, index = false)
    private String bizType;

    /**
     * 操作类型.
     */
    @Schema(description = "操作类型")
    @Field(type = FieldType.Text, index = false)
    private String operateType;

    /**
     * 操作名称.
     */
    @Schema(description = "操作名称")
    @Field(type = FieldType.Text, index = false)
    private String operateName;

    /**
     * 操作描述.
     */
    @Schema(description = "操作描述")
    @Field(type = FieldType.Text, index = false)
    private String operateDesc;

    /**
     * 请求参数.
     */
    @Schema(description = "请求参数")
    @Field(type = FieldType.Text, index = false)
    private String requestParam;

    /**
     * 请求体.
     */
    @Schema(description = "请求体")
    @Field(type = FieldType.Text, index = false)
    private String requestBody;

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
    @Field(type = FieldType.Text, index = false)
    private String requestTime;

    /**
     * 响应时间.
     */
    @Schema(description = "响应时间")
    @Field(type = FieldType.Text, index = false)
    private String responseTime;

    /**
     * 执行时间（ms）.
     */
    @Schema(description = "执行时间（ms）")
    @Field(type = FieldType.Long)
    private Long executeTime;

    /**
     * 平台名称.
     */
    @Schema(description = "平台名称")
    @Field(type = FieldType.Text, index = false)
    private String platformName;

    /**
     * 系统名称.
     */
    @Schema(description = "系统名称")
    @Field(type = FieldType.Text, index = false)
    private String osName;

    /**
     * 浏览器名称.
     */
    @Schema(description = "浏览器名称")
    @Field(type = FieldType.Text, index = false)
    private String browserName;

    /**
     * 跟踪唯一标识.
     */
    @Schema(description = "跟踪唯一标识")
    @Field(type = FieldType.Keyword)
    private String bizTraceId;

    /**
     * 请求ip.
     */
    @Schema(description = "请求ip")
    @Field(type = FieldType.Text, index = false)
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
