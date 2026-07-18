/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.vo;

import com.iwindplus.base.domain.vo.DbVersionBaseTwoVO;
import com.iwindplus.im.domain.enums.MsgTypeEnum;
import com.iwindplus.im.domain.enums.SendStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 系统通知消息视图对象.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Schema(description = "系统通知消息视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SysNoticeMsgVO extends DbVersionBaseTwoVO {
    /**
     * 标题.
     */
    @Schema(description = "标题")
    private String title;

    /**
     * 消息内容.
     */
    @Schema(description = "消息内容")
    private String content;

    /**
     * 消息类型.
     */
    @Schema(description = "消息类型")
    private MsgTypeEnum msgType;

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
