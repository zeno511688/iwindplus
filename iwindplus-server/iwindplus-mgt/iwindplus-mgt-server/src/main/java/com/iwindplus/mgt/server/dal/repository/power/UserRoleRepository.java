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
import com.iwindplus.mgt.domain.dto.power.UserRoleDTO;
import com.iwindplus.mgt.server.dal.mapper.power.UserRoleMapper;
import com.iwindplus.mgt.server.dal.model.power.UserRoleDO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户角色聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class UserRoleRepository extends JoinCrudRepository<UserRoleMapper, UserRoleDO> {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean save(UserRoleDTO entity) {
        List<UserRoleDO> entities = new ArrayList<>(10);
        UserRoleDO model = BeanUtil.copyProperties(entity, UserRoleDO.class);
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
     * @param userId  用户主键
     * @param roleIds 角色主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchRole(Long userId, Set<Long> roleIds) {
        if (Objects.isNull(userId) || CollUtil.isEmpty(roleIds)) {
            return Boolean.FALSE;
        }
        List<UserRoleDO> entities = new ArrayList<>(10);
        roleIds.stream().forEach(roleId -> {
            UserRoleDO entity = UserRoleDO.builder()
                .userId(userId)
                .roleId(roleId)
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
     * @param roleId  角色主键
     * @param userIds 用户主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchUser(Long roleId, Set<Long> userIds) {
        if (Objects.isNull(roleId) || CollUtil.isEmpty(userIds)) {
            return Boolean.FALSE;
        }
        List<UserRoleDO> entities = new ArrayList<>(10);
        userIds.stream().forEach(userId -> {
            UserRoleDO entity = UserRoleDO.builder()
                .userId(userId)
                .roleId(roleId)
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
    public boolean saveBatch(List<UserRoleDTO> entities) {
        if (CollUtil.isEmpty(entities)) {
            return Boolean.FALSE;
        }
        List<UserRoleDO> params = new ArrayList<>(10);
        entities.stream().forEach(entity -> {
            UserRoleDO model = BeanUtil.copyProperties(entity, UserRoleDO.class);
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
     * @param userId  用户主键
     * @param roleIds 角色主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean editBatchRole(Long userId, Set<Long> roleIds) {
        if (Objects.isNull(userId)) {
            return Boolean.FALSE;
        }
        super.getBaseMapper().deleteByUserIds(Arrays.asList(userId));
        this.saveBatchRole(userId, roleIds);
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param roleId  角色主键
     * @param userIds 用户主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean editBatchUser(Long roleId, Set<Long> userIds) {
        if (Objects.isNull(roleId)) {
            return Boolean.FALSE;
        }
        super.getBaseMapper().deleteByRoleIds(Arrays.asList(roleId));
        this.saveBatchUser(roleId, userIds);
        return Boolean.TRUE;
    }

    private void buildParam(UserRoleDO entity, List<UserRoleDO> entities) {
        long count = super.count(Wrappers.lambdaQuery(UserRoleDO.class)
            .eq(UserRoleDO::getUserId, entity.getUserId())
            .eq(UserRoleDO::getRoleId, entity.getRoleId()));
        if (!SqlHelper.retBool(count)) {
            entities.add(entity);
        }
    }
}
