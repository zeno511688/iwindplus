/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.vo;

import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import com.iwindplus.im.domain.enums.ChatGroupStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 聊天群分页视图对象.
 *
 * @author zengdegui
 * @since 2023/11/08 21:22
 */
@Schema(description = "聊天群分页视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGroupPageVO extends DbVersionBaseVO {

    /**
     * 状态（BAN：封禁，NORMAL：正常）.
     */
    @Schema(description = "状态（BAN：封禁，NORMAL：正常）")
    private ChatGroupStatusEnum status;

    /**
     * 群名称.
     */
    @Schema(description = "群名称")
    private String groupName;
}
