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
 * 资源基础字段扩展视图对象（标记选中）.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "资源基础字段扩展视图对象（标记选中）")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceBaseCheckedVO extends ResourceBaseVO {

    /**
     * 是否选中（false:否 true：是）.
     */
    @Schema(description = "是否选中（false:否 true：是）")
    private Boolean checked;

    /**
     * 类型.
     */
    @Schema(description = "类型")
    private String type;

    /**
     * 排序号.
     */
    @Schema(description = "排序号")
    private Integer seq;

    /**
     * 菜单主键.
     */
    @Schema(description = "菜单主键")
    private Long menuId;
}