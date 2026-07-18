/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.dto;

import com.iwindplus.base.domain.dto.DbPageDTO;
import com.iwindplus.flow.domain.enums.FlowTaskStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 审批记录搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2026/05/20
 */
@Schema(description = "审批记录搜索数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowApprovalRecordSearchDTO extends DbPageDTO {

    /**
     * 实例主键.
     */
    @Schema(description = "实例主键")
    private Long instanceId;

    /**
     * 任务名称.
     */
    @Schema(description = "任务名称")
    private String taskName;

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
     * 任务状态.
     */
    @Schema(description = "任务状态（AUDITED：已审核，REJECTED：已驳回，REVOKED：已撤销，TERMINATED：已终止）")
    private FlowTaskStatusEnum status;

    /**
     * 是否只看我的（true：仅查询当前用户参与的记录）.
     */
    @Schema(description = "是否只看我的")
    private Boolean onlyMine;

    /**
     * 用户主键
     */
    @Schema(description = "用户主键")
    private Long userId;
}