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
 * 登录日志.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Schema(description = "登录日志对象")
@Document(indexName = "login_log")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LoginLogDO extends EsDbBaseDO {

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
     * 模块名称.
     */
    @Schema(description = "模块名称")
    @Field(type = FieldType.Keyword)
    private String moduleName;

    /**
     * 模块描述.
     */
    @Schema(description = "模块描述")
    @Field(type = FieldType.Text, index = false)
    private String moduleDesc;

    /**
     * 平台名称.
     */
    @Schema(description = "平台名称")
    @Field(type = FieldType.Text)
    private String platformName;

    /**
     * 系统名称.
     */
    @Schema(description = "系统名称")
    @Field(type = FieldType.Text)
    private String osName;

    /**
     * 系统版本.
     */
    @Schema(description = "系统版本")
    @Field(type = FieldType.Keyword)
    private String osVersion;

    /**
     * 浏览器名称.
     */
    @Schema(description = "浏览器名称")
    @Field(type = FieldType.Text)
    private String browserName;

    /**
     * 浏览器版本.
     */
    @Schema(description = "浏览器版本")
    @Field(type = FieldType.Keyword)
    private String browserVersion;

    /**
     * 设备号.
     */
    @Schema(description = "设备号")
    @Field(type = FieldType.Keyword)
    private String deviceNumber;

    /**
     * 设备版本.
     */
    @Schema(description = "设备版本")
    @Field(type = FieldType.Keyword)
    private String deviceVersion;

    /**
     * 设备指纹.
     */
    @Schema(description = "设备指纹")
    @Field(type = FieldType.Keyword)
    private String deviceFingerprint;

    /**
     * ip.
     */
    @Schema(description = "ip")
    @Field(type = FieldType.Keyword)
    private String ip;

    /**
     * 省份.
     */
    @Schema(description = "省份")
    @Field(type = FieldType.Keyword)
    private String province;

    /**
     * 城市.
     */
    @Schema(description = "城市")
    @Field(type = FieldType.Keyword)
    private String city;

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
