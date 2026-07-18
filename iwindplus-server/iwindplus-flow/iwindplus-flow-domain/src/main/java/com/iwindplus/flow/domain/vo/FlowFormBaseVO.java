/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 流程表单基础字段视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "流程表单基础字段视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowFormBaseVO implements Serializable {

    /**
     * 主键.
     */
    @Schema(description = "主键")
    private Long id;

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
}