/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power;

import com.iwindplus.mgt.domain.dto.power.UserDepartmentDTO;
import java.util.List;
import java.util.Set;

/**
 * 用户部门关系业务层接口类.
 *
 * @author zengdegui
 * @since 2026/01/15
 */
public interface UserDepartmentService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(UserDepartmentDTO entity);

    /**
     * 添加.
     *
     * @param userId      用户主键
     * @param departmentIds 部门主键集合
     * @return boolean
     */
    boolean saveBatchUser(Long userId, Set<Long> departmentIds);

    /**
     * 添加.
     *
     * @param departmentId 部门主键
     * @param userIds    用户主键集合
     * @return boolean
     */
    boolean saveBatchDepartment(Long departmentId, Set<Long> userIds);

    /**
     * 添加.
     *
     * @param entities 对象集合
     * @return boolean
     */
    boolean saveBatch(List<UserDepartmentDTO> entities);

    /**
     * 编辑.
     *
     * @param userId      用户主键
     * @param departmentIds 部门主键集合
     * @return boolean
     */
    boolean editBatchUser(Long userId, Set<Long> departmentIds);

    /**
     * 编辑.
     *
     * @param departmentId 部门主键
     * @param userIds    用户主键集合
     * @return boolean
     */
    boolean editBatchDepartment(Long departmentId, Set<Long> userIds);

    /**
     * 真实删除.
     *
     * @param ids 主键集合
     * @return boolean
     */
    boolean removeByIds(List<Long> ids);

    /**
     * 通过用户主键真实删除.
     *
     * @param userIds 用户主键集合
     * @return boolean
     */
    boolean removeByUserIds(List<Long> userIds);

    /**
     * 通过部门主键真实删除.
     *
     * @param departmentIds 部门主键集合
     * @return boolean
     */
    boolean removeByDepartmentIds(List<Long> departmentIds);
}
