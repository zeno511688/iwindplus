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
 * 邮件日志.
 *
 * @author zengdegui
 * @since 2020/4/27
 */
@Schema(description = "邮件日志对象")
@Document(indexName = "mail_log")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MailLogDO extends EsDbBaseDO {

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
     * 主题.
     */
    @Schema(description = "主题")
    @Field(type = FieldType.Keyword)
    private String subject;

    /**
     * 内容（必填）.
     */
    @Schema(description = "内容")
    @Field(type = FieldType.Text, index = false)
    private String content;

    /**
     * 发件服务器账户.
     */
    @Schema(description = "发件服务器账户")
    @Field(type = FieldType.Text, index = false)
    private String username;

    /**
     * 发件人昵称.
     */
    @Schema(description = "发件人昵称")
    @Field(type = FieldType.Keyword)
    private String nickName;

    /**
     * 收件人（必填）.
     */
    @Schema(description = "收件人")
    @Field(type = FieldType.Keyword)
    private String tos;

    /**
     * 抄送人.
     */
    @Schema(description = "抄送人")
    @Field(type = FieldType.Text, index = false)
    private String ccs;

    /**
     * 密送人.
     */
    @Schema(description = "密送人")
    @Field(type = FieldType.Text, index = false)
    private String bccs;

    /**
     * 发送次数.
     */
    @Schema(description = "发送次数")
    @Field(type = FieldType.Integer)
    private Integer sendCount;

    /**
     * 错误信息.
     */
    @Schema(description = "错误信息")
    @Field(type = FieldType.Text, index = false)
    private String errorMsg;

    /**
     * 发送结果（0：失败，1：成功）.
     */
    @Schema(description = "发送结果（false：失败，true：成功）")
    @Field(type = FieldType.Boolean, index = false)
    private Boolean result;

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
