/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.dto;

import com.iwindplus.base.domain.enums.ConditionTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 条件表达式数据传输对象.
 *
 * @author zengdegui
 * @since 2025/09/09 23:13
 */
@Schema(description = "条件表达式数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ConditionExpressionDTO implements Serializable {

    /**
     * 名称.
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 字段.
     */
    @Schema(description = "字段")
    private String field;

    /**
     * 操作.
     */
    @Schema(description = "操作")
    private String operator;

    /**
     * 值.
     */
    @Schema(description = "值")
    private String value;

    /**
     * 条件类型.
     */
    @Schema(description = "条件类型")
    private ConditionTypeEnum type;
}
