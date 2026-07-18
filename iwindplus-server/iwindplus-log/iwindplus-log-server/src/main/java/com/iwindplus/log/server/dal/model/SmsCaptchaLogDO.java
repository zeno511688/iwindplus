/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.server.dal.model;

import com.iwindplus.base.es.domain.EsDbBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 短信验证码日志.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "短信验证码日志对象")
@Document(indexName = "sms_captcha_log")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SmsCaptchaLogDO extends EsDbBaseDO {

    /**
     * 请求唯一标识.
     */
    @Schema(description = "请求唯一标识")
    @Field(type = FieldType.Keyword)
    private String requestId;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    @Field(type = FieldType.Keyword)
    private String bizNumber;

    /**
     * 短信模板编码.
     */
    @Schema(description = "短信模板编码")
    @Field(type = FieldType.Keyword)
    private String tplCode;

    /**
     * 手机.
     */
    @Schema(description = "手机")
    @Field(type = FieldType.Keyword)
    private String mobile;

    /**
     * 验证码.
     */
    @Schema(description = "验证码")
    @Field(type = FieldType.Keyword)
    private String captcha;

    /**
     * 过期时间.
     */
    @Schema(description = "过期时间")
    @Field(
        type = FieldType.Date,
        format = {},
        pattern = "yyyy-MM-dd HH:mm:ss",
        index = false
    )
    private LocalDateTime expireTime;

    /**
     * 是否使用（false：未使用，true：已使用）
     */
    @Schema(description = "是否使用（false：未使用，true：已使用）")
    @Field(type = FieldType.Boolean, index = false)
    private Boolean used;

    /**
     * 使用时间.
     */
    @Schema(description = "使用时间")
    @Field(
        type = FieldType.Date,
        format = {},
        pattern = "yyyy-MM-dd HH:mm:ss",
        index = false
    )
    private LocalDateTime useTime;

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