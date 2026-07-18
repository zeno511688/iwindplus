/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.power;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

/**
 * 角色修改数据传输对象.
 *
 * @author zengdegui
 * @since 2019/8/23
 */
@Schema(description = "角色修改数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RoleEditDTO extends RoleDTO {

    /**
     * 菜单主键集合.
     */
    @Schema(description = "菜单主键集合")
    private Set<Long> menuIds;

    /**
     * 资源主键集合.
     */
    @Schema(description = "资源主键集合")
    private Set<Long> resourceIds;
}
