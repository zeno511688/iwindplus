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
 * 流程实例回调外部应用数据传输对象.
 *
 * @author zengdegui
 * @since 2026/01/11 20:14
 */
@Schema(description = "流程实例回调外部应用数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowInstanceCallbackExtDTO implements Serializable {

    /**
     * 流程实例状态.
     */
    @Schema(description = "流程实例状态")
    private FlowInstanceStatusEnum instanceStatus;

    /**
     * 实例主键.
     */
    @Schema(description = "实例主键")
    private Long instanceId;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

    /**
     * 模型编码.
     */
    @Schema(description = "模型编码")
    private String modelCode;

    /**
     * 变量.
     */
    @Schema(description = "变量")
    private String variable;
}
