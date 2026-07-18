/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.dto;

import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.flow.domain.enums.FlowTaskPlayerTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 流程任务参与人数据传输对象.
 *
 * @author zengdegui
 * @since 2026/01/11 20:14
 */
@Schema(description = "流程任务参与人数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowTaskPlayerDTO extends DbVersionBaseDTO {

    /**
     * 参与人名称.
     */
    @Schema(description = "参与人名称")
    private String playerName;

    /**
     * 任务参与人类型 （0：用户 1：角色）.
     */
    private FlowTaskPlayerTypeEnum type;

    /**
     * 排序号.
     */
    @Schema(description = "排序号")
    private Integer seq;

    /**
     * 参与人主键.
     */
    @Schema(description = "参与人主键")
    private Long playerId;

    /**
     * 任务主键.
     */
    @Schema(description = "任务主键")
    private Long taskId;

    /**
     * 实例主键.
     */
    @Schema(description = "实例主键")
    private Long instanceId;

    /**
     * 模型主键.
     */
    @Schema(description = "模型主键")
    private Long modelId;
}
