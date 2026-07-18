/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.server.dal.model;

import com.iwindplus.base.es.domain.EsDbBaseDO;
import com.iwindplus.im.domain.enums.MsgTypeEnum;
import com.iwindplus.im.domain.enums.SendStatusEnum;
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
 * 系统通知消息表.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Schema(description = "系统通知消息对象")
@Document(indexName = "sys_notice_msg")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SysNoticeMsgDO extends EsDbBaseDO {

    /**
     * 标题.
     */
    @Schema(description = "标题")
    @Field(type = FieldType.Keyword)
    private String title;

    /**
     * 消息内容.
     */
    @Schema(description = "消息内容")
    @Field(type = FieldType.Keyword)
    private String content;

    /**
     * 消息类型.
     */
    @Schema(description = "消息类型")
    @Field(type = FieldType.Keyword)
    private MsgTypeEnum msgType;

    /**
     * 发送人主键.
     */
    @Schema(description = "发送人主键")
    @Field(type = FieldType.Long)
    private Long senderId;

    /**
     * 发送人头像.
     */
    @Schema(description = "发送人头像")
    @Field(type = FieldType.Text, index = false)
    private String senderAvatar;

    /**
     * 发送人昵称.
     */
    @Schema(description = "发送人昵称")
    @Field(type = FieldType.Keyword)
    private String senderNickName;

    /**
     * 发送时间.
     */
    @Schema(description = "发送时间")
    @Field(
        type = FieldType.Date,
        format = {},
        pattern = "yyyy-MM-dd HH:mm:ss",
        index = false
    )
    private LocalDateTime sendTime;

    /**
     * 发送状态
     */
    @Schema(description = "发送状态")
    @Field(type = FieldType.Keyword)
    private SendStatusEnum sendStatus;

    /**
     * 排序号.
     */
    @Schema(description = "排序号")
    @Field(type = FieldType.Integer)
    private Integer seq;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    @Field(type = FieldType.Long)
    private Long orgId;
}
