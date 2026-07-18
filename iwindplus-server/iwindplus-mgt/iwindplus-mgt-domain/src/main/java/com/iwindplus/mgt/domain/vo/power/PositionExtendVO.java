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
 * 职位扩展视图对象.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Schema(description = "职位扩展视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PositionExtendVO extends PositionVO {

    /**
     * 组织编码.
     */
    @Schema(description = "组织编码")
    private String orgCode;

    /**
     * 组织名称.
     */
    @Schema(description = "组织名称")
    private String orgName;

    /**
     * 组织简称.
     */
    @Schema(description = "组织简称")
    private String orgAbbr;

    /**
     * 部门编码.
     */
    @Schema(description = "部门编码")
    private String departmentCode;

    /**
     * 部门编码.
     */
    @Schema(description = "部门编码")
    private String departmentName;
}
