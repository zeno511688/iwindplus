/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power;

import com.iwindplus.mgt.domain.dto.power.UserPositionDTO;
import java.util.List;
import java.util.Set;

/**
 * 用户职位关系业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface UserPositionService {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean save(UserPositionDTO entity);

    /**
     * 添加.
     *
     * @param userId      用户主键
     * @param positionIds 职位主键集合
     * @return boolean
     */
    boolean saveBatchPosition(Long userId, Set<Long> positionIds);

    /**
     * 添加.
     *
     * @param positionId 职位主键
     * @param userIds    用户主键集合
     * @return boolean
     */
    boolean saveBatchUser(Long positionId, Set<Long> userIds);

    /**
     * 添加.
     *
     * @param entities 对象集合
     * @return boolean
     */
    boolean saveBatch(List<UserPositionDTO> entities);

    /**
     * 编辑.
     *
     * @param userId      用户主键
     * @param positionIds 职位主键集合
     * @return boolean
     */
    boolean editBatchPosition(Long userId, Set<Long> positionIds);

    /**
     * 编辑.
     *
     * @param positionId 职位主键
     * @param userIds    用户主键集合
     * @return boolean
     */
    boolean editBatchUser(Long positionId, Set<Long> userIds);
}
