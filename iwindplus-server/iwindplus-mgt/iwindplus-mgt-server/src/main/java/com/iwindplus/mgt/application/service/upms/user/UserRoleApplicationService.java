/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.upms.user;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.mgt.application.service.upms.user.dto.UserRoleDTO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserRoleDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserRoleRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户角色关系业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_USER_ROLE})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class UserRoleApplicationService {

    private final UserRoleRepository userRoleRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    public boolean save(UserRoleDTO entity) {
        List<UserRoleDO> entities = new ArrayList<>(10);
        UserRoleDO model = BeanUtil.copyProperties(entity, UserRoleDO.class);
        this.buildParam(model, entities);
        if (CollUtil.isNotEmpty(entities)) {
            this.userRoleRepository.saveBatch(entities, Constants.DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    public boolean saveBatchRole(Long userId, Set<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return Boolean.FALSE;
        }
        List<UserRoleDO> entities = new ArrayList<>(10);
        roleIds.stream().filter(Objects::nonNull).forEach(roleId -> {
            UserRoleDO entity = UserRoleDO.builder()
                .userId(userId)
                .roleId(roleId)
                .build();
            this.buildParam(entity, entities);
        });
        if (CollUtil.isNotEmpty(entities)) {
            this.userRoleRepository.saveBatch(entities, Constants.DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    public boolean saveBatchUser(Long roleId, Set<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Boolean.FALSE;
        }
        List<UserRoleDO> entities = new ArrayList<>(10);
        userIds.stream().filter(Objects::nonNull).forEach(userId -> {
            UserRoleDO entity = UserRoleDO.builder()
                .userId(userId)
                .roleId(roleId)
                .build();
            this.buildParam(entity, entities);
        });
        if (CollUtil.isNotEmpty(entities)) {
            this.userRoleRepository.saveBatch(entities, Constants.DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    public boolean saveBatch(List<UserRoleDTO> entities) {
        if (CollUtil.isEmpty(entities)) {
            return Boolean.FALSE;
        }
        List<UserRoleDO> params = new ArrayList<>(10);
        entities.stream().filter(Objects::nonNull).forEach(entity -> {
            UserRoleDO model = BeanUtil.copyProperties(entity, UserRoleDO.class);
            this.buildParam(model, params);
        });
        if (CollUtil.isNotEmpty(params)) {
            this.userRoleRepository.saveBatch(params, Constants.DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    public boolean editBatchRole(Long userId, Set<Long> roleIds) {
        this.userRoleRepository.getBaseMapper().deleteByUserIds(List.of(userId));
        this.saveBatchRole(userId, roleIds);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    public boolean editBatchUser(Long roleId, Set<Long> userIds) {
        this.userRoleRepository.getBaseMapper().deleteByRoleIds(List.of(roleId));
        this.saveBatchUser(roleId, userIds);
        return Boolean.TRUE;
    }

    private void buildParam(UserRoleDO entity, List<UserRoleDO> entities) {
        long count = this.userRoleRepository.count(Wrappers.lambdaQuery(UserRoleDO.class)
            .eq(UserRoleDO::getUserId, entity.getUserId())
            .eq(UserRoleDO::getRoleId, entity.getRoleId()));
        if (!SqlHelper.retBool(count)) {
            entities.add(entity);
        }
    }
}
