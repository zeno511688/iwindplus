/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.model.power;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iwindplus.base.mybatis.domain.DbBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 角色菜单关系表.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "角色菜单关系对象")
@TableName(value = "`role_menu`")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RoleMenuDO extends DbBaseDO {

    /**
     * 角色主键.
     */
    @Schema(description = "角色主键")
    private Long roleId;

    /**
     * 菜单主键.
     */
    @Schema(description = "菜单主键")
    private Long menuId;
}