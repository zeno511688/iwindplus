/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 聊天群基础字段视图对象.
 *
 * @author zengdegui
 * @since 2020/4/14
 */
@Schema(description = "聊天群基础字段视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGroupBaseVO implements Serializable {

    /**
     * 主键.
     */
    @Schema(description = "主键")
    private Long id;

    /**
     * 群名称.
     */
    @Schema(description = "群名称")
    private String groupName;
}
