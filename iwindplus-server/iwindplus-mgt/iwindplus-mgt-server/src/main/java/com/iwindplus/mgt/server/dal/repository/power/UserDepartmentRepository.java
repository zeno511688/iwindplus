/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.repository.power;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.github.yulichang.repository.JoinCrudRepository;
import com.iwindplus.mgt.server.dal.mapper.power.UserDepartmentMapper;
import com.iwindplus.mgt.server.dal.model.power.UserDepartmentDO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户部门聚合层接口类.
 *
 * @author zengdegui
 * @since 2026/01/15
 */
@Repository
public class UserDepartmentRepository extends JoinCrudRepository<UserDepartmentMapper, UserDepartmentDO> {

    /**
     * 通过用户主键查询.
     *
     * @param userId 用户主键
     * @return List<UserDepartmentDO>
     */
    public List<UserDepartmentDO> listByUserId(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return this.list(
            Wrappers.lambdaQuery(UserDepartmentDO.class)
                .eq(UserDepartmentDO::getUserId, userId)
                .orderByDesc(UserDepartmentDO::getPrimaryFlag)
        );
    }

    /**
     * 通过部门主键查询.
     *
     * @param departmentId 部门主键
     * @return List<UserDepartmentDO>
     */
    public List<UserDepartmentDO> listByDepartmentId(Long departmentId) {
        if (departmentId == null) {
            return new ArrayList<>();
        }
        return this.list(
            Wrappers.lambdaQuery(UserDepartmentDO.class)
                .eq(UserDepartmentDO::getDepartmentId, departmentId)
        );
    }

    /**
     * 批量保存用户部门关系.
     *
     * @param userId        用户主键
     * @param departmentIds 部门主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchDepartment(Long userId, Set<Long> departmentIds) {
        if (userId == null || CollUtil.isEmpty(departmentIds)) {
            return false;
        }
        List<UserDepartmentDO> entities = new ArrayList<>(departmentIds.size());
        // 将Set转换为List，以便确定第一个元素作为主要部门
        List<Long> departmentIdList = departmentIds.stream()
            .filter(Objects::nonNull)
            .toList();

        for (int i = 0; i < departmentIdList.size(); i++) {
            Long departmentId = departmentIdList.get(i);
            UserDepartmentDO entity = UserDepartmentDO.builder()
                .userId(userId)
                .departmentId(departmentId)
                .primaryFlag(i == 0)
                .build();
            this.buildParam(entity, entities);
        }
        if (CollUtil.isNotEmpty(entities)) {
            return this.saveBatch(entities, Constants.DEFAULT_BATCH_SIZE);
        }
        return false;
    }

    /**
     * 批量保存部门用户关系.
     *
     * @param departmentId 部门主键
     * @param userIds      用户主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchUser(Long departmentId, Set<Long> userIds) {
        if (departmentId == null || CollUtil.isEmpty(userIds)) {
            return false;
        }
        List<UserDepartmentDO> entities = new ArrayList<>(userIds.size());
        // 将Set转换为List，以便确定第一个元素作为主要部门
        List<Long> userIdList = userIds.stream()
            .filter(Objects::nonNull)
            .toList();

        for (int i = 0; i < userIdList.size(); i++) {
            Long userId = userIdList.get(i);
            UserDepartmentDO entity = UserDepartmentDO.builder()
                .userId(userId)
                .departmentId(departmentId)
                .primaryFlag(i == 0)
                .build();
            this.buildParam(entity, entities);
        }
        if (CollUtil.isNotEmpty(entities)) {
            return this.saveBatch(entities, Constants.DEFAULT_BATCH_SIZE);
        }
        return false;
    }

    /**
     * 编辑用户部门关系.
     *
     * @param userId        用户主键
     * @param departmentIds 部门主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean editBatchDepartment(Long userId, Set<Long> departmentIds) {
        if (userId == null) {
            return false;
        }
        // 先删除旧的关联
        this.getBaseMapper().deleteByUserIds(Arrays.asList(userId));
        // 再保存新的关联
        return this.saveBatchDepartment(userId, departmentIds);
    }

    /**
     * 编辑部门用户关系.
     *
     * @param departmentId 部门主键
     * @param userIds      用户主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean editBatchUser(Long departmentId, Set<Long> userIds) {
        if (departmentId == null) {
            return false;
        }
        // 先删除旧的关联
        this.getBaseMapper().deleteByDepartmentIds(Arrays.asList(departmentId));
        // 再保存新的关联
        return this.saveBatchUser(departmentId, userIds);
    }

    /**
     * 通过用户主键真实删除.
     *
     * @param userIds 用户主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByUserIds(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return false;
        }
        return SqlHelper.retBool(this.getBaseMapper().deleteByUserIds(userIds));
    }

    /**
     * 通过部门主键真实删除.
     *
     * @param departmentIds 部门主键集合
     * @return boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByDepartmentIds(List<Long> departmentIds) {
        if (CollUtil.isEmpty(departmentIds)) {
            return false;
        }
        return SqlHelper.retBool(this.getBaseMapper().deleteByDepartmentIds(departmentIds));
    }

    /**
     * 构建参数.
     *
     * @param entity   实体对象
     * @param entities 实体集合
     */
    private void buildParam(UserDepartmentDO entity, List<UserDepartmentDO> entities) {
        long count = super.count(Wrappers.lambdaQuery(UserDepartmentDO.class)
            .eq(UserDepartmentDO::getUserId, entity.getUserId())
            .eq(UserDepartmentDO::getDepartmentId, entity.getDepartmentId()));
        if (!SqlHelper.retBool(count)) {
            entities.add(entity);
        }
    }
}
