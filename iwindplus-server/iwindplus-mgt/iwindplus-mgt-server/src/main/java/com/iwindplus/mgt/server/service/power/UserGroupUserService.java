/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power;

import com.iwindplus.mgt.domain.dto.power.UserGroupUserDTO;
import java.util.List;
import java.util.Set;

/**
 * 用户组用户关系业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface UserGroupUserService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(UserGroupUserDTO entity);

    /**
     * 添加.
     *
     * @param userId       用户主键
     * @param userGroupIds 用户组主键集合
     * @return boolean
     */
    boolean saveBatchUserGroup(Long userId, Set<Long> userGroupIds);

    /**
     * 添加.
     *
     * @param userGroupId 用户组主键
     * @param userIds     用户主键集合
     * @return boolean
     */
    boolean saveBatchUser(Long userGroupId, Set<Long> userIds);

    /**
     * 添加.
     *
     * @param entities 对象集合
     * @return boolean
     */
    boolean saveBatch(List<UserGroupUserDTO> entities);

    /**
     * 编辑.
     *
     * @param userId       用户主键
     * @param userGroupIds 用户组主键集合
     * @return boolean
     */
    boolean editBatchUserGroup(Long userId, Set<Long> userGroupIds);

    /**
     * 编辑.
     *
     * @param userGroupId 用户组主键
     * @param userIds     用户主键集合
     * @return boolean
     */
    boolean editBatchUser(Long userGroupId, Set<Long> userIds);
}
