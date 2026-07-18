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
 * 角色基础字段视图对象（标记选中）.
 *
 * @author zengdegui
 * @since 2018/9/17
 */
@Schema(description = "角色基础字段视图对象（标记选中）")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RoleBaseCheckedVO extends RoleBaseVO {

    /**
     * 是否选中（false:否 true：是）.
     */
    @Schema(description = "是否选中（false:否 true：是）")
    private Boolean checked;
}
