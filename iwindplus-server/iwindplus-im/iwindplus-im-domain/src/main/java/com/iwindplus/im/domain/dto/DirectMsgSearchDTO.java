/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.dto;

import com.iwindplus.im.domain.enums.MsgStatusEnum;
import com.iwindplus.im.domain.enums.SendStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 直发消息搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "直发消息搜索数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DirectMsgSearchDTO implements Serializable {

    /**
     * 接收人主键（必填）.
     */
    @Schema(description = "接收人主键（必填）")
    private Long receiverId;

    /**
     * 发送人主键（必填）.
     */
    @Schema(description = "发送人主键（必填）")
    private Long senderId;

    /**
     * 发送状态
     */
    @Schema(description = "发送状态")
    private SendStatusEnum sendStatus;

    /**
     * 消息状态（UN_READ：未读，READ：已读，RECYCLED：已回收）.
     */
    @Schema(description = "消息状态（UN_READ：未读，READ：已读，RECYCLED：已回收）")
    private MsgStatusEnum msgStatus;

    /**
     * 当前登录用户主键.
     */
    @Schema(description = "当前登录用户主键")
    private Long currentUserId;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}
