/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power.impl;

import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.power.UserGroupRoleDTO;
import com.iwindplus.mgt.server.dal.repository.power.UserGroupRoleRepository;
import com.iwindplus.mgt.server.service.power.UserGroupRoleService;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户组角色关系业务层接口实现类.
 *
 * @author zengdegui
 * @since 2019/10/9
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_ROLE})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class UserGroupRoleServiceImpl implements UserGroupRoleService {

    private final UserGroupRoleRepository userGroupRoleRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean save(UserGroupRoleDTO entity) {
        return this.userGroupRoleRepository.save(entity);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean saveBatchUserGroup(Long roleId, Set<Long> userGroupIds) {
        return this.userGroupRoleRepository.saveBatchUserGroup(roleId, userGroupIds);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean saveBatchRole(Long userGroupId, Set<Long> roleIds) {
        return this.userGroupRoleRepository.saveBatchRole(userGroupId, roleIds);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean saveBatch(List<UserGroupRoleDTO> entities) {
        return this.userGroupRoleRepository.saveBatch(entities);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean editBatchUserGroup(Long roleId, Set<Long> userGroupIds) {
        return this.userGroupRoleRepository.editBatchUserGroup(roleId, userGroupIds);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean editBatchRole(Long userGroupId, Set<Long> roleIds) {
        return this.userGroupRoleRepository.editBatchRole(userGroupId, roleIds);
    }

}
