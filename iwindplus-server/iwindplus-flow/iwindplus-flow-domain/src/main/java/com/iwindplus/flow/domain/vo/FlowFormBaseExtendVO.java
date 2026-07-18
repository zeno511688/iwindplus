/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 流程表单基础字段扩展视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "流程表单基础字段扩展视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowFormBaseExtendVO extends FlowFormBaseVO {

    /**
     * 内容
     */
    @Schema(description = "内容")
    private String content;
}