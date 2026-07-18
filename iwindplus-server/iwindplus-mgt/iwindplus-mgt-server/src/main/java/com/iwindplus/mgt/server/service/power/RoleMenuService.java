/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.power;

import com.iwindplus.mgt.domain.dto.power.RoleMenuDTO;
import java.util.List;
import java.util.Set;

/**
 * 角色菜单关系业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface RoleMenuService {

    /**
     * 保存.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(RoleMenuDTO entity);

    /**
     * 保存.
     *
     * @param roleId  角色主键
     * @param menuIds 菜单主键集合
     * @return boolean
     */
    boolean saveBatchMenu(Long roleId, Set<Long> menuIds);

    /**
     * 保存.
     *
     * @param menuId  菜单主键
     * @param roleIds 角色主键集合
     * @return boolean
     */
    boolean saveBatchRole(Long menuId, Set<Long> roleIds);

    /**
     * 保存.
     *
     * @param entities 对象集合
     * @return boolean
     */
    boolean saveBatch(List<RoleMenuDTO> entities);

    /**
     * 编辑.
     *
     * @param roleId  角色主键
     * @param menuIds 菜单主键集合
     * @return boolean
     */
    boolean editBatchMenu(Long roleId, Set<Long> menuIds);

    /**
     * 编辑.
     *
     * @param menuId  菜单主键
     * @param roleIds 角色主键集合
     * @return boolean
     */
    boolean editBatchRole(Long menuId, Set<Long> roleIds);
}
