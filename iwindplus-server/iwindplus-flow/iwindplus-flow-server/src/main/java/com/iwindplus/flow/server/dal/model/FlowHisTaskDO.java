/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */
package com.iwindplus.flow.server.dal.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iwindplus.flow.domain.enums.FlowTaskStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 历史流程任务表.
 *
 * @author zengdegui
 * @since 2021/7/8
 */
@Schema(description = "历史流程任务实体对象")
@TableName(value = "`flow_his_task`")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowHisTaskDO extends FlowTaskDO {

    /**
     * 任务状态.
     */
    @Schema(description = "任务状态")
    private FlowTaskStatusEnum status;

    /**
     * 耗时.
     */
    @Schema(description = "耗时")
    private Long takeTime;
}
