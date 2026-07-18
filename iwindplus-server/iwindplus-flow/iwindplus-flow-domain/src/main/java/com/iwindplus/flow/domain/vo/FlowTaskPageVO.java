/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.vo;

import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import com.iwindplus.flow.domain.enums.FlowTaskTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 我的审批分页视图对象.
 *
 * @author zengdegui
 * @since 2026/05/20 23:43
 */
@Schema(description = "我的审批分页视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowTaskPageVO extends DbVersionBaseVO {

    /**
     * 编码.
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 节点编码.
     */
    @Schema(description = "节点编码")
    private String nodeCode;

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
     * 当前节点名称.
     */
    @Schema(description = "当前节点名称")
    private String currentNodeName;

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
