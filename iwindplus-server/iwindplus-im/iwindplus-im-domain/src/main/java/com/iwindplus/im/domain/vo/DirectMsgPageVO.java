/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.vo;

import com.iwindplus.base.domain.vo.DbVersionBaseTwoVO;
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

/**
 * 直发消息分页视图对象.
 *
 * @author zengdegui
 * @since 2023/11/08 21:22
 */
@Schema(description = "直发消息分页视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DirectMsgPageVO extends DbVersionBaseTwoVO {
    /**
     * 标题.
     */
    @Schema(description = "标题")
    private String title;

    /**
     * 消息类型.
     */
    @Schema(description = "消息类型")
    private MsgTypeEnum msgType;

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
}
