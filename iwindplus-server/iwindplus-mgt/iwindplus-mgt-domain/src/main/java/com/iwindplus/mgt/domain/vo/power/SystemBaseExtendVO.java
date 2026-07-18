/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.power;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 系统基础字段扩展视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "系统基础字段扩展视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SystemBaseExtendVO extends SystemBaseVO {

    /**
     * 排序号.
     */
    @Schema(description = "排序号")
    private Integer seq;

}