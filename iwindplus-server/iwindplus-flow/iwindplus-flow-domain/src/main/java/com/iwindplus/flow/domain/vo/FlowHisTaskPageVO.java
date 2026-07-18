/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.vo;

import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import com.iwindplus.flow.domain.enums.FlowTaskStatusEnum;
import com.iwindplus.flow.domain.enums.FlowTaskTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 历史流程任务分页视图对象.
 *
 * @author zengdegui
 * @since 2026/05/20 23:40
 */
@Schema(description = "历史流程任务分页视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowHisTaskPageVO extends DbVersionBaseVO {

    /**
     * 编码.
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 任务名称.
     */
    @Schema(description = "任务名称")
    private String name;

    /**
     * 任务类型.
     */
    @Schema(description = "任务类型")
    private FlowTaskTypeEnum type;

    /**
     * 任务状态.
     */
    @Schema(description = "任务状态（AUDITED：已审核，REJECTED：已驳回，REVOKED：已撤销，TERMINATED：已终止）")
    private FlowTaskStatusEnum status;

    /**
     * 耗时（毫秒）.
     */
    @Schema(description = "耗时（毫秒）")
    private Long takeTime;

    /**
     * 实例主键.
     */
    @Schema(description = "实例主键")
    private Long instanceId;

    /**
     * 实例名称.
     */
    @Schema(description = "实例名称")
    private String instanceName;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

    /**
     * 模型主键.
     */
    @Schema(description = "模型主键")
    private Long modelId;

    /**
     * 模型名称.
     */
    @Schema(description = "模型名称")
    private String modelName;

    /**
     * 模型编码.
     */
    @Schema(description = "模型编码")
    private String modelCode;
}
