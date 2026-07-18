/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.dto;

import com.iwindplus.flow.domain.enums.ApprovalMethodEnum;
import com.iwindplus.flow.domain.enums.ApprovalTypeEnum;
import com.iwindplus.flow.domain.enums.FlowNodeTypeEnum;
import com.iwindplus.flow.domain.enums.FlowTaskPlayerTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程节点数据传输对象.
 *
 * @author zengdegui
 * @since 2024/12/30 23:30
 */
@Schema(description = "流程节点数据传输对象")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowNodeDTO implements Serializable {

    /**
     * 编码.
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 节点类型.
     */
    @Schema(description = "节点类型")
    private FlowNodeTypeEnum nodeType;

    /**
     * 参与人类型.
     */
    @Schema(description = "参与人类型")
    private FlowTaskPlayerTypeEnum playerType;

    /**
     * 审批方式.
     */
    @Schema(description = "审批方式")
    private ApprovalMethodEnum approvalMethod;

    /**
     * 审批类型.
     */
    @Schema(description = "审批类型")
    private ApprovalTypeEnum approvalType;

    /**
     * 子节点.
     */
    @Schema(description = "子节点")
    private FlowNodeDTO childNode;

    /**
     * 通过权重（用于票签）.
     */
    @Schema(description = "通过权重（用于票签）")
    private Integer passWeight;

    /**
     * 节点参与人.
     */
    @Schema(description = "节点参与人")
    private List<FlowNodePlayerDTO> nodePlayers;

    /**
     * 条件节点集合
     */
    @Schema(description = "条件节点集合")
    private List<FlowConditionNodeDTO> conditionNodes;

}
