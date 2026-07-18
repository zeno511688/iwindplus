/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.dto;

import com.iwindplus.base.domain.dto.ConditionExpressionDTO;
import com.iwindplus.flow.domain.enums.FlowNodeTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程条件节点数据传输对象.
 *
 * @author zengdegui
 * @since 2024/12/30 23:30
 */
@Schema(description = "流程条件节点数据传输对象")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowConditionNodeDTO implements Serializable {

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
     * 优先级.
     */
    @Schema(description = "优先级")
    private Integer priority;

    /**
     * 子节点.
     */
    @Schema(description = "子节点")
    private FlowNodeDTO childNode;

    /**
     * 节点条件表达式集合.
     */
    @Schema(description = "节点条件表达式集合")
    private List<ConditionExpressionDTO> conditions;
}
