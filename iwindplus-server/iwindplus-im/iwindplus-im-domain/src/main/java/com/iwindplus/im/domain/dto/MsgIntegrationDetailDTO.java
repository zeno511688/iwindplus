/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.dto;

import com.iwindplus.im.domain.enums.CommandEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 消息集成详情搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "消息集成详情数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MsgIntegrationDetailDTO implements Serializable {
    /**
     * 条数.
     */
    @Schema(description = "条数")
    private Integer size;

    /**
     * 主键
     */
    @Schema(description = "主键")
    private String id;

    /**
     * 指令.
     */
    @Schema(description = "指令")
    private  CommandEnum command;

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
