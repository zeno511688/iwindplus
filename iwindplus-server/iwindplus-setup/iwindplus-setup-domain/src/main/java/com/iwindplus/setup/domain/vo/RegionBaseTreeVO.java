/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 省市区基础字段视图对象（树形）.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "省市区基础字段视图对象（树形）")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RegionBaseTreeVO extends RegionBaseVO {

    /**
     * 级别.
     */
    @Schema(description = "级别")
    private Integer level;

    /**
     * 排序号.
     */
    @Schema(description = "排序号")
    private Integer seq;

    /**
     * 父类主键.
     */
    @Schema(description = "父类主键")
    private Long parentId;
}