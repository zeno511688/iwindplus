/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.vo;

import com.iwindplus.flow.domain.dto.FlowModelContentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 流程模型视图对象.
 *
 * @author zengdegui
 * @since 2021/9/23
 */
@Schema(description = "流程模型视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowModelExtVO extends FlowModelVO {

    /**
     * 模型内容
     */
    @Schema(description = "模型内容")
    private FlowModelContentDTO modelContent;

    /**
     * 表单内容
     */
    @Schema(description = "表单内容")
    private String formContent;
}
