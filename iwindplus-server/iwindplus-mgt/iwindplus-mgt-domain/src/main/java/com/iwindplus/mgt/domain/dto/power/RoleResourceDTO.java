/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.power;

import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 角色资源关系数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "角色资源关系数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResourceDTO extends DbVersionBaseDTO {

    /**
     * 角色主键.
     */
    @Schema(description = "角色主键")
    @NotNull(message = "{roleId.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private Long roleId;

    /**
     * 菜单主键.
     */
    @Schema(description = "菜单主键")
    private Long menuId;

    /**
     * 资源主键.
     */
    @Schema(description = "资源主键")
    @NotNull(message = "{resourceId.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private Long resourceId;
}