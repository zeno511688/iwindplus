/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.dto;

import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 流程模型编辑数据传输对象.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Schema(description = "流程模型编辑数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowModelEditDTO extends FlowModelDTO {

    /**
     * 模型内容
     */
    @Schema(description = "模型内容")
    @NotNull(message = "{modelContent.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Valid
    private FlowModelContentDTO modelContent;
}
