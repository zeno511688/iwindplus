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
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 我的已办搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2026/05/20 23:50
 */
@Schema(description = "我的已办搜索数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowHisTaskSearchDTO extends DbPageDTO {

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
     * 角色ID集合（用于角色类型任务匹配）.
     */
    @Schema(description = "角色ID集合")
    private List<Long> roleIds;

    /**
     * 用户主键
     */
    @Schema(description = "用户主键")
    private Long userId;
}
