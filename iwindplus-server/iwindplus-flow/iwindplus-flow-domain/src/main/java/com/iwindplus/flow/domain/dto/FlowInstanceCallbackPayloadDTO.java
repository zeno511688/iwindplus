/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.dto;

import com.iwindplus.flow.domain.enums.FlowInstanceStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 流程实例回调对象.
 *
 * @author zengdegui
 * @since 2026/05/21 01:30
 */
@Schema(description = "流程实例回调对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowInstanceCallbackPayloadDTO implements Serializable {

    /**
     * 流程实例ID
     */
    @Schema(description = "流程实例ID")
    private Long instanceId;

    /**
     * 流程实例编号
     */
    @Schema(description = "流程实例编号")
    private String instanceCode;

    /**
     * 业务编号
     */
    @Schema(description = "业务编号")
    private String bizNumber;

    /**
     * 流程实例状态
     */
    @Schema(description = "流程实例状态")
    private FlowInstanceStatusEnum status;

    /**
     * 流程变量
     */
    @Schema(description = "流程变量")
    private String variable;
}
