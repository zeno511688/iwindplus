/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.server.dal.model;

import com.iwindplus.base.es.domain.EsDbBaseDO;
import com.iwindplus.im.domain.enums.MsgStatusEnum;
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
 * 直发消息表.
 *
 * @author zengdegui
 * @since 2023/11/08 21:22
 */
@Schema(description = "直发消息实体对象")
@Document(indexName = "direct_msg")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DirectMsgDO extends EsDbBaseDO {

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
    @Field(type = FieldType.Text, index = false)
    private MsgTypeEnum msgType;

    /**
     * 接收人主键.
     */
    @Schema(description = "接收人主键")
    @Field(type = FieldType.Long)
    private Long receiverId;

    /**
     * 接收人头像.
     */
    @Schema(description = "接收人头像")
    @Field(type = FieldType.Text, index = false)
    private String receiverAvatar;

    /**
     * 接收人昵称.
     */
    @Schema(description = "接收人昵称")
    @Field(type = FieldType.Keyword)
    private String receiverNickName;

    /**
     * 消息状态（UN_READ：未读，READ：已读，RECYCLED：已回收）.
     */
    @Schema(description = "消息状态（UN_READ：未读，READ：已读，RECYCLED：已回收）")
    @Field(type = FieldType.Keyword)
    private MsgStatusEnum msgStatus;

    /**
     * 读取时间.
     */
    @Schema(description = "读取时间")
    @Field(
        type = FieldType.Date,
        format = {},
        pattern = "yyyy-MM-dd HH:mm:ss",
        index = false
    )
    private LocalDateTime readTime;

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
    @Field(type = FieldType.Text, index = false)
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
