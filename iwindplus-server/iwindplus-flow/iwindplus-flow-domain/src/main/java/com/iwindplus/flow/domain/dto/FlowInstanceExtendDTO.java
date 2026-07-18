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
 * 流程实例扩展数据传输对象.
 *
 * @author zengdegui
 * @since 2026/01/11 20:14
 */
@Schema(description = "流程实例扩展数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowInstanceExtendDTO extends DbVersionBaseDTO {

    /**
     * 变量
     */
    @Schema(description = "变量")
    private String variable;

    /**
     * 实例主键.
     */
    @Schema(description = "实例主键")
    @NotNull(message = "{instanceId.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private Long instanceId;
}
