/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power;

import com.iwindplus.mgt.domain.dto.power.UserGroupRoleDTO;
import java.util.List;
import java.util.Set;

/**
 * 用户组角色关系业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface UserGroupRoleService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(UserGroupRoleDTO entity);

    /**
     * 添加.
     *
     * @param roleId       角色主键
     * @param userGroupIds 用户组主键集合
     * @return boolean
     */
    boolean saveBatchUserGroup(Long roleId, Set<Long> userGroupIds);

    /**
     * 添加.
     *
     * @param userGroupId 用户组主键
     * @param roleIds     角色主键集合
     * @return boolean
     */
    boolean saveBatchRole(Long userGroupId, Set<Long> roleIds);

    /**
     * 添加.
     *
     * @param entities 对象集合
     * @return boolean
     */
    boolean saveBatch(List<UserGroupRoleDTO> entities);

    /**
     * 编辑.
     *
     * @param roleId       角色主键
     * @param userGroupIds 用户组主键集合
     * @return boolean
     */
    boolean editBatchUserGroup(Long roleId, Set<Long> userGroupIds);

    /**
     * 编辑.
     *
     * @param userGroupId 用户组主键
     * @param roleIds     角色主键集合
     * @return boolean
     */
    boolean editBatchRole(Long userGroupId, Set<Long> roleIds);
}
