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
 * 消息数据传输对象.
 *
 * @author zengdegui
 * @since 2023/12/04 23:13
 */
@Schema(description = "消息数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WsMsgDTO extends WsMsgBaseDTO {

    /**
     * 标题.
     */
    @Schema(description = "标题")
    private String title;

    /**
     * 发送人用户主键（必填）.
     */
    @Schema(description = "发送人用户主键")
    private Long sendUserId;

    /**
     * 发送人组织主键.
     */
    @Schema(description = "发送人组织主键")
    private Long sendOrgId;
}
