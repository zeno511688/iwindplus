/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.upms.permission;

import com.iwindplus.mgt.application.service.upms.permission.dto.RoleMenuDTO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.RoleMenuRepository;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 角色菜单关系业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class RoleMenuApplicationService {

    private final RoleMenuRepository roleMenuRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
        }
    )
    public boolean save(RoleMenuDTO entity) {
        return roleMenuRepository.save(entity);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
        }
    )
    public boolean saveBatchMenu(Long roleId, Set<Long> menuIds) {
        return roleMenuRepository.saveBatchMenu(roleId, menuIds);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
        }
    )
    public boolean saveBatchRole(Long menuId, Set<Long> roleIds) {
        return roleMenuRepository.saveBatchRole(menuId, roleIds);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
        }
    )
    public boolean saveBatch(List<RoleMenuDTO> entities) {
        return roleMenuRepository.saveBatch(entities);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
        }
    )
    public boolean editBatchMenu(Long roleId, Set<Long> menuIds) {
        return roleMenuRepository.editBatchMenu(roleId, menuIds);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
        }
    )
    public boolean editBatchRole(Long menuId, Set<Long> roleIds) {
        return roleMenuRepository.editBatchRole(menuId, roleIds);
    }

}
