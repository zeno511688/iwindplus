/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.power;

import com.iwindplus.base.domain.vo.BaseTreeVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 菜单字段视图对象（树形）.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "菜单字段视图对象（树形）")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MenuTreeVO extends BaseTreeVO {

    /**
     * 图标样式.
     */
    @Schema(description = "图标样式")
    private String iconStyle;

    /**
     * 图标路径（绝对路径）.
     */
    @Schema(description = "图标路径（绝对路径）")
    private String iconUrlStr;

    /**
     * 路由路径.
     */
    @Schema(description = "路由路径")
    private String routeUrl;
}