/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.dto;

import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.DbVersionBaseTwoVO;
import com.iwindplus.im.domain.enums.MsgStatusEnum;
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
 * 加好友消息数据传输对象.
 *
 * @author zengdegui
 * @since 2023/11/08 21:22
 */
@Schema(description = "加好友消息数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AddFriendMsgDTO extends DbVersionBaseTwoVO {
    /**
     * 消息内容.
     */
    @Schema(description = "消息内容")
    @NotBlank(message = "{content.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 5000, message = "{content.length}", groups = {SaveGroup.class, EditGroup.class})
    private String content;

    /**
     * 审核时间.
     */
    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    /**
     * 接收人主键.
     */
    @Schema(description = "接收人主键")
    @NotNull(message = "{receiverId.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private Long receiverId;

    /**
     * 接收人头像.
     */
    @Schema(description = "接收人头像")
    private String receiverAvatar;

    /**
     * 接收人昵称.
     */
    @Schema(description = "接收人昵称")
    private String receiverNickName;

    /**
     * 消息状态（UN_READ：未读，READ：已读，RECYCLED：已回收）.
     */
    @Schema(description = "消息状态（UN_READ：未读，READ：已读，RECYCLED：已回收）")
    private MsgStatusEnum msgStatus;

    /**
     * 读取时间.
     */
    @Schema(description = "读取时间")
    private LocalDateTime readTime;

    /**
     * 发送人主键.
     */
    @Schema(description = "发送人主键")
    @NotNull(message = "{senderId.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
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
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}
