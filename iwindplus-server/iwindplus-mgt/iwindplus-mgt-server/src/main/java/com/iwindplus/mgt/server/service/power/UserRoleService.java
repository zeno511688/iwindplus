/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power;

import com.iwindplus.mgt.domain.dto.power.UserRoleDTO;
import java.util.List;
import java.util.Set;

/**
 * 用户角色关系业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface UserRoleService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(UserRoleDTO entity);

    /**
     * 添加.
     *
     * @param userId  用户主键
     * @param roleIds 角色主键集合
     * @return boolean
     */
    boolean saveBatchRole(Long userId, Set<Long> roleIds);

    /**
     * 添加.
     *
     * @param roleId  角色主键
     * @param userIds 用户主键集合
     * @return boolean
     */
    boolean saveBatchUser(Long roleId, Set<Long> userIds);

    /**
     * 添加.
     *
     * @param entities 对象集合
     * @return boolean
     */
    boolean saveBatch(List<UserRoleDTO> entities);

    /**
     * 编辑.
     *
     * @param userId  用户主键
     * @param roleIds 角色主键集合
     * @return boolean
     */
    boolean editBatchRole(Long userId, Set<Long> roleIds);

    /**
     * 编辑.
     *
     * @param roleId  角色主键
     * @param userIds 用户主键集合
     * @return boolean
     */
    boolean editBatchUser(Long roleId, Set<Long> userIds);
}
