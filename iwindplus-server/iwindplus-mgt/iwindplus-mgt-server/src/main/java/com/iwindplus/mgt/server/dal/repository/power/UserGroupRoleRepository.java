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
import com.iwindplus.mgt.domain.dto.power.UserGroupRoleDTO;
import com.iwindplus.mgt.server.dal.mapper.power.UserGroupRoleMapper;
import com.iwindplus.mgt.server.dal.model.power.UserGroupRoleDO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户组角色关系聚合层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Repository
public class UserGroupRoleRepository extends JoinCrudRepository<UserGroupRoleMapper, UserGroupRoleDO> {

    /**
     * 添加.
     *
     * @param entity 对象
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean save(UserGroupRoleDTO entity) {
        List<UserGroupRoleDO> entities = new ArrayList<>(10);
        final UserGroupRoleDO model = BeanUtil.copyProperties(entity, UserGroupRoleDO.class);
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
     * @param roleId       角色主键
     * @param userGroupIds 用户组主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchUserGroup(Long roleId, Set<Long> userGroupIds) {
        if (Objects.isNull(roleId) || CollUtil.isEmpty(userGroupIds)) {
            return Boolean.FALSE;
        }
        List<UserGroupRoleDO> entities = new ArrayList<>(10);
        userGroupIds.stream().forEach(userGroupId -> {
            UserGroupRoleDO entity = UserGroupRoleDO.builder()
                .userGroupId(userGroupId)
                .roleId(roleId)
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
     * @param roleIds     角色主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchRole(Long userGroupId, Set<Long> roleIds) {
        if (Objects.isNull(userGroupId) || CollUtil.isEmpty(roleIds)) {
            return Boolean.FALSE;
        }
        List<UserGroupRoleDO> entities = new ArrayList<>(10);
        roleIds.stream().forEach(roleId -> {
            UserGroupRoleDO entity = UserGroupRoleDO.builder()
                .userGroupId(userGroupId)
                .roleId(roleId)
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
    public boolean saveBatch(List<UserGroupRoleDTO> entities) {
        if (CollUtil.isEmpty(entities)) {
            return Boolean.FALSE;
        }
        List<UserGroupRoleDO> params = new ArrayList<>(10);
        entities.stream().forEach(entity -> {
            UserGroupRoleDO model = BeanUtil.copyProperties(entity, UserGroupRoleDO.class);
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
     * @param roleId       角色主键
     * @param userGroupIds 用户组主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean editBatchUserGroup(Long roleId, Set<Long> userGroupIds) {
        if (Objects.isNull(roleId)) {
            return Boolean.FALSE;
        }
        super.getBaseMapper().deleteByRoleIds(Arrays.asList(roleId));
        this.saveBatchUserGroup(roleId, userGroupIds);
        return Boolean.TRUE;
    }

    /**
     * 编辑.
     *
     * @param userGroupId 用户组主键
     * @param roleIds     角色主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean editBatchRole(Long userGroupId, Set<Long> roleIds) {
        if (Objects.isNull(userGroupId)) {
            return Boolean.FALSE;
        }
        super.getBaseMapper().deleteByUserGroupIds(Arrays.asList(userGroupId));
        this.saveBatchRole(userGroupId, roleIds);
        return Boolean.TRUE;
    }

    private void buildParam(UserGroupRoleDO entity, List<UserGroupRoleDO> entities) {
        long count = super.count(Wrappers.lambdaQuery(UserGroupRoleDO.class)
            .eq(UserGroupRoleDO::getUserGroupId, entity.getUserGroupId())
            .eq(UserGroupRoleDO::getRoleId, entity.getRoleId()));
        if (!SqlHelper.retBool(count)) {
            entities.add(entity);
        }
    }
}
