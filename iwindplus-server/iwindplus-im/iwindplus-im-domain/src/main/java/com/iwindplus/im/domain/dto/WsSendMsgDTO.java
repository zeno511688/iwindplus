/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.dto;

import com.iwindplus.im.domain.enums.CommandEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 消息推送数据传输对象.
 *
 * @author zengdegui
 * @since 2023/12/04 23:13
 */
@Schema(description = "消息推送数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WsSendMsgDTO extends WsMsgDTO {

    /**
     * 指令.
     */
    @Schema(description = "指令")
    @NotNull(message = "{command.notEmpty}")
    private CommandEnum command;

    /**
     * 消息主键.
     */
    @Schema(description = "消息主键")
    private String msgId;
}
