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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 流程模型数据传输对象.
 *
 * @author zengdegui
 * @since 2021/7/8
 */
@Schema(description = "流程模型数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowModelExtendDTO extends DbVersionBaseDTO {

    /**
     * 模型内容
     */
    @Schema(description = "模型内容")
    @NotBlank(message = "{modelContent.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private FlowModelContentDTO modelContent;

    /**
     * 表单内容
     */
    @Schema(description = "表单内容")
    private String formContent;

    /**
     * 模型主键.
     */
    @Schema(description = "模型主键")
    @NotNull(message = "{modelId.notEmpty}", groups = {SaveGroup.class})
    private Long modelId;
}
