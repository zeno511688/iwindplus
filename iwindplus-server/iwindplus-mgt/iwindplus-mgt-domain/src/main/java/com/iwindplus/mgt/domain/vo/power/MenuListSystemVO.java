/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.power;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 菜单字段集合视图对象-系统纬度.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "菜单字段集合视图对象-系统纬度")
@Data
@EqualsAndHashCode(callSuper=false)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MenuListSystemVO extends SystemBaseExtendVO {

    /**
     * 菜单集合.
     */
    @Schema(description = "菜单集合")
    private List<MenuTreeVO> menus;
}