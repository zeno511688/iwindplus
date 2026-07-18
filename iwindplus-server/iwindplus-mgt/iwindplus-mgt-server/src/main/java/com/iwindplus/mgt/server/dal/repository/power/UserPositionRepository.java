/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.power;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.mgt.domain.dto.power.UserPositionDTO;
import com.iwindplus.mgt.server.dal.mapper.power.UserPositionMapper;
import com.iwindplus.mgt.server.dal.model.power.UserPositionDO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户职位聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class UserPositionRepository extends JoinCrudRepository<UserPositionMapper, UserPositionDO> {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean save(UserPositionDTO entity) {
        List<UserPositionDO> entities = new ArrayList<>(10);
        UserPositionDO model = BeanUtil.copyProperties(entity, UserPositionDO.class);
        this.buildParam(model, entities);
        if (CollUtil.isNotEmpty(entities)) {
            super.saveBatch(entities, Constants.DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 添加.
     *
     * @param userId      用户主键
     * @param positionIds 职位主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchPosition(Long userId, Set<Long> positionIds) {
        if (Objects.isNull(userId) || CollUtil.isEmpty(positionIds)) {
            return Boolean.FALSE;
        }
        List<UserPositionDO> entities = new ArrayList<>(10);
        positionIds.stream().forEach(positionId -> {
            UserPositionDO entity = UserPositionDO.builder()
                .userId(userId)
                .positionId(positionId)
                .build();
            this.buildParam(entity, entities);
        });
        if (CollUtil.isNotEmpty(entities)) {
            super.saveBatch(entities, Constants.DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 添加.
     *
     * @param positionId 职位主键
     * @param userIds    用户主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchUser(Long positionId, Set<Long> userIds) {
        if (Objects.isNull(positionId) || CollUtil.isEmpty(userIds)) {
            return Boolean.FALSE;
        }
        List<UserPositionDO> entities = new ArrayList<>(10);
        userIds.stream().forEach(userId -> {
            UserPositionDO entity = UserPositionDO.builder()
                .userId(userId)
                .positionId(positionId)
                .build();
            this.buildParam(entity, entities);
        });
        if (CollUtil.isNotEmpty(entities)) {
            super.saveBatch(entities, Constants.DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 添加.
     *
     * @param entities 对象集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatch(List<UserPositionDTO> entities) {
        if (CollUtil.isEmpty(entities)) {
            return Boolean.FALSE;
        }
        List<UserPositionDO> params = new ArrayList<>(10);
        entities.stream().forEach(entity -> {
            UserPositionDO model = BeanUtil.copyProperties(entity, UserPositionDO.class);
            this.buildParam(model, params);
        });
        if (CollUtil.isNotEmpty(params)) {
            super.saveBatch(params, Constants.DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 编辑.
     *
     * @param userId      用户主键
     * @param positionIds 职位主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean editBatchPosition(Long userId, Set<Long> positionIds) {
        if (Objects.isNull(userId)) {
            return Boolean.FALSE;
        }
        super.getBaseMapper().deleteByUserIds(Arrays.asList(userId));
        this.saveBatchPosition(userId, positionIds);
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param positionId 职位主键
     * @param userIds    用户主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean editBatchUser(Long positionId, Set<Long> userIds) {
        if (Objects.isNull(positionId)) {
            return Boolean.FALSE;
        }
        super.getBaseMapper().deleteByPositionIds(Arrays.asList(positionId));
        this.saveBatchUser(positionId, userIds);
        return Boolean.TRUE;
    }

    private void buildParam(UserPositionDO entity, List<UserPositionDO> entities) {
        long count = super.count(Wrappers.lambdaQuery(UserPositionDO.class)
            .eq(UserPositionDO::getUserId, entity.getUserId())
            .eq(UserPositionDO::getPositionId, entity.getPositionId()));
        if (!SqlHelper.retBool(count)) {
            entities.add(entity);
        }
    }
}
