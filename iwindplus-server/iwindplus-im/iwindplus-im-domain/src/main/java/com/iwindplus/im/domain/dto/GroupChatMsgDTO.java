/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.dto;

import com.iwindplus.base.domain.dto.DbVersionBaseTwoDTO;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.im.domain.enums.MsgTypeEnum;
import com.iwindplus.im.domain.enums.SendStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 群聊消息数据传输对象.
 *
 * @author zengdegui
 * @since 2023/11/08 21:22
 */
@Schema(description = "群聊消息数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GroupChatMsgDTO extends DbVersionBaseTwoDTO {
    /**
     * 消息内容.
     */
    @Schema(description = "消息内容")
    @NotBlank(message = "{content.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 5000, message = "{content.length}", groups = {SaveGroup.class, EditGroup.class})
    private String content;

    /**
     * 消息类型.
     */
    @Schema(description = "消息类型")
    @NotNull(message = "{msgType.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private MsgTypeEnum msgType;

    /**
     * 聊天群主键.
     */
    @Schema(description = "聊天群主键")
    @NotNull(message = "{chatGroupId.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private Long chatGroupId;

    /**
     * 发送人主键.
     */
    @Schema(description = "发送人主键")
    private Long senderId;

    /**
     * 发送人头像.
     */
    @Schema(description = "发送人头像")
    private String senderAvatar;

    /**
     * 发送人昵称.
     */
    @Schema(description = "发送人昵称")
    private String senderNickName;

    /**
     * 发送时间.
     */
    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

    /**
     * 发送状态
     */
    @Schema(description = "发送状态")
    private SendStatusEnum sendStatus;

    /**
     * 排序号.
     */
    @Schema(description = "排序号")
    private Integer seq;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}
