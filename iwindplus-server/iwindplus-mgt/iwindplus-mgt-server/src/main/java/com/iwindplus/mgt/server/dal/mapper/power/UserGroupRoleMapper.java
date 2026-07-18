/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.mapper.power;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.github.yulichang.base.MPJBaseMapper;
import com.iwindplus.mgt.server.dal.model.power.UserGroupRoleDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户组角色关系访问层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Mapper
public interface UserGroupRoleMapper extends MPJBaseMapper<UserGroupRoleDO> {

    /**
     * 真实删除.
     *
     * @param ids 主键集合
     * @return int
     */
    int deleteByIds(@Param(Constants.LIST) List<Long> ids);

    /**
     * 通过用户组主键真实删除.
     *
     * @param userGroupIds 用户组主键集合
     * @return int
     */
    int deleteByUserGroupIds(@Param(Constants.LIST) List<Long> userGroupIds);

    /**
     * 通过角色主键真实删除.
     *
     * @param roleIds 角色主键集合
     * @return int
     */
    int deleteByRoleIds(@Param(Constants.LIST) List<Long> roleIds);
}
