/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 聊天群用户添加数据传输对象.
 *
 * @author zengdegui
 * @since 2023/11/09 21:19
 */
@Schema(description = "聊天群用户添加数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGroupUserSaveDTO extends ChatGroupUserDTO {
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
