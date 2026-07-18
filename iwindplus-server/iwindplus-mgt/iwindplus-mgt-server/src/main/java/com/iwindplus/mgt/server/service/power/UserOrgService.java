/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power;

import com.iwindplus.mgt.domain.dto.power.UserOrgDTO;
import java.util.List;
import java.util.Set;

/**
 * 用户组织关系业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface UserOrgService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(UserOrgDTO entity);

    /**
     * 添加.
     *
     * @param orgId   组织主键
     * @param userIds 用户主键集合
     * @return boolean
     */
    boolean saveBatchUser(Long orgId, Set<Long> userIds);

    /**
     * 添加.
     *
     * @param userId 用户主键
     * @param orgIds 组织主键集合
     * @return boolean
     */
    boolean saveBatchOrg(Long userId, Set<Long> orgIds);

    /**
     * 添加.
     *
     * @param entities 对象集合
     * @return boolean
     */
    boolean saveBatch(List<UserOrgDTO> entities);

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
     * 通过组织主键真实删除.
     *
     * @param orgIds 组织主键集合
     * @return boolean
     */
    boolean removeByOrgIds(List<Long> orgIds);

    /**
     * 编辑.
     *
     * @param orgId   组织主键
     * @param userIds 用户主键集合
     * @return boolean
     */
    boolean editBatchUser(Long orgId, Set<Long> userIds);

    /**
     * 编辑.
     *
     * @param userId 用户主键
     * @param orgIds 组织主键集合
     * @return boolean
     */
    boolean editBatchOrg(Long userId, Set<Long> orgIds);

    /**
     * 编辑设为选中.
     *
     * @param newUserOrgId 新选中用户组织关系主键
     * @param oldUserOrgId 旧选中用户组织关系主键
     * @return boolean
     */
    boolean editChecked(Long newUserOrgId, Long oldUserOrgId);

    /**
     * 校验用户是否切换过组织.
     *
     * @param userId 用户主键
     * @param orgId  组织主键
     * @return boolean
     */
    boolean checkChangeOrg(Long userId, Long orgId);
}
