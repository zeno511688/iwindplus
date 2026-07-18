/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power.impl;

import static com.baomidou.mybatisplus.extension.repository.IRepository.DEFAULT_BATCH_SIZE;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.power.UserGroupUserDTO;
import com.iwindplus.mgt.server.dal.model.power.UserGroupUserDO;
import com.iwindplus.mgt.server.dal.repository.power.UserGroupUserRepository;
import com.iwindplus.mgt.server.service.power.UserGroupUserService;
import java.util.ArrayList;
import java.util.Arrays;
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
 * 用户组用户关系业务层接口实现类.
 *
 * @author zengdegui
 * @since 2019/10/9
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_USER})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class UserGroupUserServiceImpl implements UserGroupUserService {

    private final UserGroupUserRepository userGroupUserRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean save(UserGroupUserDTO entity) {
        List<UserGroupUserDO> entities = new ArrayList<>(10);
        UserGroupUserDO model = BeanUtil.copyProperties(entity, UserGroupUserDO.class);
        this.buildParam(model, entities);
        if (CollUtil.isNotEmpty(entities)) {
            this.userGroupUserRepository.saveBatch(entities, DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean saveBatchUserGroup(Long userId, Set<Long> userGroupIds) {
        if (CollUtil.isEmpty(userGroupIds)) {
            return Boolean.FALSE;
        }
        List<UserGroupUserDO> entities = new ArrayList<>(10);
        userGroupIds.stream().filter(Objects::nonNull).forEach(userGroupId -> {
            UserGroupUserDO entity = UserGroupUserDO.builder()
                .userId(userId)
                .userGroupId(userGroupId)
                .build();
            this.buildParam(entity, entities);
        });
        if (CollUtil.isNotEmpty(entities)) {
            this.userGroupUserRepository.saveBatch(entities, DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean saveBatchUser(Long userGroupId, Set<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Boolean.FALSE;
        }
        List<UserGroupUserDO> entities = new ArrayList<>(10);
        userIds.stream().filter(Objects::nonNull).forEach(userId -> {
            UserGroupUserDO entity = UserGroupUserDO.builder()
                .userId(userId)
                .userGroupId(userGroupId)
                .build();
            this.buildParam(entity, entities);
        });
        if (CollUtil.isNotEmpty(entities)) {
            this.userGroupUserRepository.saveBatch(entities, DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean saveBatch(List<UserGroupUserDTO> entities) {
        if (CollUtil.isEmpty(entities)) {
            return Boolean.FALSE;
        }
        List<UserGroupUserDO> params = new ArrayList<>(10);
        entities.stream().filter(Objects::nonNull).forEach(entity -> {
            UserGroupUserDO model = BeanUtil.copyProperties(entity, UserGroupUserDO.class);
            this.buildParam(model, params);
        });
        if (CollUtil.isNotEmpty(params)) {
            this.userGroupUserRepository.saveBatch(params, DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean editBatchUserGroup(Long userId, Set<Long> userGroupIds) {
        this.userGroupUserRepository.getBaseMapper().deleteByUserIds(Arrays.asList(userId));
        this.saveBatchUserGroup(userId, userGroupIds);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean editBatchUser(Long userGroupId, Set<Long> userIds) {
        this.userGroupUserRepository.getBaseMapper().deleteByUserGroupIds(Arrays.asList(userGroupId));
        this.saveBatchUser(userGroupId, userIds);
        return Boolean.TRUE;
    }

    private void buildParam(UserGroupUserDO entity, List<UserGroupUserDO> entities) {
        long count = this.userGroupUserRepository.count(Wrappers.lambdaQuery(UserGroupUserDO.class)
            .eq(UserGroupUserDO::getUserId, entity.getUserId())
            .eq(UserGroupUserDO::getUserGroupId, entity.getUserGroupId()));
        if (!SqlHelper.retBool(count)) {
            entities.add(entity);
        }
    }
}
