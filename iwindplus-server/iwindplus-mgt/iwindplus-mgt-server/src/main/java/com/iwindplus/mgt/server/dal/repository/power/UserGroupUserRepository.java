/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.power;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.mgt.domain.dto.power.UserGroupUserDTO;
import com.iwindplus.mgt.server.dal.mapper.power.UserGroupUserMapper;
import com.iwindplus.mgt.server.dal.model.power.UserGroupUserDO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户组用户关系聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class UserGroupUserRepository extends JoinCrudRepository<UserGroupUserMapper, UserGroupUserDO> {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean save(UserGroupUserDTO entity) {
        List<UserGroupUserDO> entities = new ArrayList<>(10);
        UserGroupUserDO model = BeanUtil.copyProperties(entity, UserGroupUserDO.class);
        this.buildParam(model, entities);
        if (CollUtil.isNotEmpty(entities)) {
            super.saveBatch(entities, DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 添加.
     *
     * @param userId       用户主键
     * @param userGroupIds 用户组主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchUserGroup(Long userId, Set<Long> userGroupIds) {
        if (Objects.isNull(userId) || CollUtil.isEmpty(userGroupIds)) {
            return Boolean.FALSE;
        }
        List<UserGroupUserDO> entities = new ArrayList<>(10);
        userGroupIds.stream().forEach(userGroupId -> {
            UserGroupUserDO entity = UserGroupUserDO.builder()
                .userId(userId)
                .userGroupId(userGroupId)
                .build();
            this.buildParam(entity, entities);
        });
        if (CollUtil.isNotEmpty(entities)) {
            super.saveBatch(entities, DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 添加.
     *
     * @param userGroupId 用户组主键
     * @param userIds     用户主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchUser(Long userGroupId, Set<Long> userIds) {
        if (Objects.isNull(userGroupId) || CollUtil.isEmpty(userIds)) {
            return Boolean.FALSE;
        }
        List<UserGroupUserDO> entities = new ArrayList<>(10);
        userIds.stream().forEach(userId -> {
            UserGroupUserDO entity = UserGroupUserDO.builder()
                .userId(userId)
                .userGroupId(userGroupId)
                .build();
            this.buildParam(entity, entities);
        });
        if (CollUtil.isNotEmpty(entities)) {
            super.saveBatch(entities, DEFAULT_BATCH_SIZE);
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
    public boolean saveBatch(List<UserGroupUserDTO> entities) {
        if (CollUtil.isEmpty(entities)) {
            return Boolean.FALSE;
        }
        List<UserGroupUserDO> params = new ArrayList<>(10);
        entities.stream().forEach(entity -> {
            UserGroupUserDO model = BeanUtil.copyProperties(entity, UserGroupUserDO.class);
            this.buildParam(model, params);
        });
        if (CollUtil.isNotEmpty(params)) {
            super.saveBatch(params, DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 编辑.
     *
     * @param userId       用户主键
     * @param userGroupIds 用户组主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean editBatchUserGroup(Long userId, Set<Long> userGroupIds) {
        if (Objects.isNull(userId)) {
            return Boolean.FALSE;
        }
        super.getBaseMapper().deleteByUserIds(Arrays.asList(userId));
        this.saveBatchUserGroup(userId, userGroupIds);
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param userGroupId 用户组主键
     * @param userIds     用户主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean editBatchUser(Long userGroupId, Set<Long> userIds) {
        if (Objects.isNull(userGroupId)) {
            return Boolean.FALSE;
        }
        super.getBaseMapper().deleteByUserGroupIds(Arrays.asList(userGroupId));
        this.saveBatchUser(userGroupId, userIds);
        return Boolean.TRUE;
    }

    private void buildParam(UserGroupUserDO entity, List<UserGroupUserDO> entities) {
        long count = super.count(Wrappers.lambdaQuery(UserGroupUserDO.class)
            .eq(UserGroupUserDO::getUserId, entity.getUserId())
            .eq(UserGroupUserDO::getUserGroupId, entity.getUserGroupId()));
        if (!SqlHelper.retBool(count)) {
            entities.add(entity);
        }
    }
}
