/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.dto;

import com.iwindplus.im.domain.enums.MsgTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 消息推送数据基础传输对象.
 *
 * @author zengdegui
 * @since 2023/12/04 23:13
 */
@Schema(description = "消息推送数据基础传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WsMsgBaseDTO implements Serializable {

    /**
     * 消息类型.
     */
    @Schema(description = "消息类型")
    @NotNull(message = "{msgType.notEmpty}")
    private MsgTypeEnum msgType;

    /**
     * 子消息类型.
     */
    @Schema(description = "子消息类型")
    private String subMsgType;

    /**
     * 消息内容.
     */
    @Schema(description = "消息内容")
    @NotNull(message = "{content.notEmpty}")
    private Object content;

    /**
     * 接收人主键.
     */
    @Schema(description = "接收人主键")
    @NotNull(message = "{receiverId.notEmpty}")
    private Long receiverId;
}
