/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power;

import com.iwindplus.mgt.domain.dto.power.RoleResourceDTO;
import java.util.List;
import java.util.Set;

/**
 * 角色资源关系业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface RoleResourceService {

    /**
     * 保存.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(RoleResourceDTO entity);

    /**
     * 保存.
     *
     * @param roleId      角色主键
     * @param resourceIds 资源主键集合
     * @return boolean
     */
    boolean saveBatchResource(Long roleId, Set<Long> resourceIds);

    /**
     * 保存.
     *
     * @param resourceId 资源主键
     * @param roleIds    角色主键集合
     * @return boolean
     */
    boolean saveBatchRole(Long resourceId, Set<Long> roleIds);

    /**
     * 保存.
     *
     * @param entities 对象集合
     * @return boolean
     */
    boolean saveBatch(List<RoleResourceDTO> entities);

    /**
     * 编辑.
     *
     * @param roleId      角色主键
     * @param resourceIds 资源主键集合
     * @return boolean
     */
    boolean editBatchResource(Long roleId, Set<Long> resourceIds);

    /**
     * 编辑.
     *
     * @param resourceId 资源主键
     * @param roleIds    角色主键集合
     * @return boolean
     */
    boolean editBatchRole(Long resourceId, Set<Long> roleIds);
}
