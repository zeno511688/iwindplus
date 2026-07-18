/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.mapper.power;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.github.yulichang.base.MPJBaseMapper;
import com.iwindplus.mgt.server.dal.model.power.RoleResourceDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色资源关系访问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Mapper
public interface RoleResourceMapper extends MPJBaseMapper<RoleResourceDO> {

    /**
     * 真实删除.
     *
     * @param ids 主键集合
     * @return int
     */
    int deleteByIds(@Param(Constants.LIST) List<Long> ids);

    /**
     * 通过角色主键真实删除.
     *
     * @param roleIds 角色主键集合
     * @return int
     */
    int deleteByRoleIds(@Param(Constants.LIST) List<Long> roleIds);

    /**
     * 通过菜单主键真实删除.
     *
     * @param menuIds 菜单主键集合
     * @return int
     */
    int deleteByMenuIds(@Param(Constants.LIST) List<Long> menuIds);

    /**
     * 通过资源主键真实删除.
     *
     * @param resourceIds 资源主键集合
     * @return boolean
     */
    int deleteByResourceIds(@Param(Constants.LIST) List<Long> resourceIds);
}
