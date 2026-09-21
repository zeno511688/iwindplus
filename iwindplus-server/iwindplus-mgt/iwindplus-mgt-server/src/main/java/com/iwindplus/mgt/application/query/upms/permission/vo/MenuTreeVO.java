/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.upms.permission.vo;

import com.iwindplus.base.domain.vo.BaseTreeVO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
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

    /**
     * 新功能标记过期时间.
     */
    @Schema(description = "新功能标记过期时间")
    private LocalDateTime newFeatureExpireTime;

    /**
     * 是否新功能（当前时间小于过期时间时为true）.
     */
    @Schema(description = "是否新功能")
    private Boolean newFeature;
}