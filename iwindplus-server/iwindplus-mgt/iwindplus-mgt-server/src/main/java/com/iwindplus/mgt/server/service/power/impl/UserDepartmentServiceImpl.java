/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.power.UserDepartmentDTO;
import com.iwindplus.mgt.server.dal.model.power.UserDepartmentDO;
import com.iwindplus.mgt.server.dal.repository.power.UserDepartmentRepository;
import com.iwindplus.mgt.server.service.power.UserDepartmentService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户部门关系业务层接口实现类.
 *
 * @author zengdegui
 * @since 2026/01/15
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_USER_DEPARTMENT})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class UserDepartmentServiceImpl implements UserDepartmentService {

    private final UserDepartmentRepository userDepartmentRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
        }
    )
    @Override
    public boolean save(UserDepartmentDTO entity) {
        List<UserDepartmentDO> entities = new ArrayList<>(10);
        UserDepartmentDO model = BeanUtil.copyProperties(entity, UserDepartmentDO.class);
        this.buildParam(model, entities);
        if (CollUtil.isNotEmpty(entities)) {
            return this.userDepartmentRepository.saveBatch(entities, Constants.DEFAULT_BATCH_SIZE);
        }
        return false;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
        }
    )
    @Override
    public boolean saveBatchUser(Long userId, Set<Long> departmentIds) {
        return this.userDepartmentRepository.saveBatchUser(userId, departmentIds);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
        }
    )
    @Override
    public boolean saveBatchDepartment(Long departmentId, Set<Long> userIds) {
        return this.userDepartmentRepository.saveBatchDepartment(departmentId, userIds);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
        }
    )
    @Override
    public boolean saveBatch(List<UserDepartmentDTO> entities) {
        if (CollUtil.isEmpty(entities)) {
            return false;
        }
        List<UserDepartmentDO> models = BeanUtil.copyToList(entities, UserDepartmentDO.class);
        return this.userDepartmentRepository.saveBatch(models, Constants.DEFAULT_BATCH_SIZE);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
        }
    )
    @Override
    public boolean editBatchUser(Long userId, Set<Long> departmentIds) {
        return this.userDepartmentRepository.editBatchUser(userId, departmentIds);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
        }
    )
    @Override
    public boolean editBatchDepartment(Long departmentId, Set<Long> userIds) {
        return this.userDepartmentRepository.editBatchDepartment(departmentId, userIds);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
        }
    )
    @Override
    public boolean removeByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        return this.userDepartmentRepository.removeByIds(ids);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
        }
    )
    @Override
    public boolean removeByUserIds(List<Long> userIds) {
        return this.userDepartmentRepository.removeByUserIds(userIds);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
        }
    )
    @Override
    public boolean removeByDepartmentIds(List<Long> departmentIds) {
        return this.userDepartmentRepository.removeByDepartmentIds(departmentIds);
    }

    /**
     * 构建参数.
     *
     * @param entity   实体对象
     * @param entities 实体集合
     */
    private void buildParam(UserDepartmentDO entity, List<UserDepartmentDO> entities) {
        if (entity == null) {
            return;
        }
        if (entities == null) {
            entities = new ArrayList<>(10);
        }
        entities.add(entity);
    }
}
