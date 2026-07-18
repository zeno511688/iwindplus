/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.dto;

import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 流程实例数据传输对象.
 *
 * @author zengdegui
 * @since 2026/01/11 20:14
 */
@Schema(description = "流程实例数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowInstanceDTO extends DbVersionBaseDTO {

    /**
     * 编码.
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    @NotNull(message = "{name.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private String name;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

    /**
     * 当前节点编码.
     */
    @Schema(description = "当前节点编码")
    private String currentNodeCode;

    /**
     * 当前节点名称
     */
    @Schema(description = "当前节点名称")
    private String currentNodeName;

    /**
     * 模型主键.
     */
    @Schema(description = "模型主键")
    @NotNull(message = "{modelId.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private Long modelId;
}
