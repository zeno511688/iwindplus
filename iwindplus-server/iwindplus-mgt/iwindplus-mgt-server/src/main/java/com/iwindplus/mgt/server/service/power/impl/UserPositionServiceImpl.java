/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.power.UserPositionDTO;
import com.iwindplus.mgt.server.dal.model.power.UserPositionDO;
import com.iwindplus.mgt.server.dal.repository.power.UserPositionRepository;
import com.iwindplus.mgt.server.service.power.UserPositionService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户职位关系业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_USER_POSITION})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class UserPositionServiceImpl implements UserPositionService {

    private final UserPositionRepository userPositionRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean save(UserPositionDTO entity) {
        List<UserPositionDO> entities = new ArrayList<>(10);
        UserPositionDO model = BeanUtil.copyProperties(entity, UserPositionDO.class);
        this.buildParam(model, entities);
        if (CollUtil.isNotEmpty(entities)) {
            this.userPositionRepository.saveBatch(entities, Constants.DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean saveBatchPosition(Long userId, Set<Long> positionIds) {
        if (CollUtil.isEmpty(positionIds)) {
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
            this.userPositionRepository.saveBatch(entities, Constants.DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean saveBatchUser(Long positionId, Set<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
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
            this.userPositionRepository.saveBatch(entities, Constants.DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
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
            this.userPositionRepository.saveBatch(params, Constants.DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean editBatchPosition(Long userId, Set<Long> positionIds) {
        this.userPositionRepository.getBaseMapper().deleteByUserIds(Arrays.asList(userId));
        this.saveBatchPosition(userId, positionIds);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean editBatchUser(Long positionId, Set<Long> userIds) {
        this.userPositionRepository.getBaseMapper().deleteByPositionIds(Arrays.asList(positionId));
        this.saveBatchUser(positionId, userIds);
        return Boolean.TRUE;
    }

    private void buildParam(UserPositionDO entity, List<UserPositionDO> entities) {
        long count = this.userPositionRepository.count(Wrappers.lambdaQuery(UserPositionDO.class)
            .eq(UserPositionDO::getUserId, entity.getUserId())
            .eq(UserPositionDO::getPositionId, entity.getPositionId()));
        if (!SqlHelper.retBool(count)) {
            entities.add(entity);
        }
    }
}
