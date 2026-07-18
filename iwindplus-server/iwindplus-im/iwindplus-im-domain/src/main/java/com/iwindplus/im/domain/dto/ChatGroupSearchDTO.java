/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 聊天群搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2023/11/09 20:20
 */
@Schema(description = "聊天群搜索数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGroupSearchDTO implements Serializable {

    /**
     * 群名称.
     */
    @Schema(description = "群名称")
    private String groupName;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    private Long userId;
}
