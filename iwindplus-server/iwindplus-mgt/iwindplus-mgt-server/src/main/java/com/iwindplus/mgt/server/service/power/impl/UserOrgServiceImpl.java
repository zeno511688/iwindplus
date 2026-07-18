/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.power.UserOrgDTO;
import com.iwindplus.mgt.server.dal.model.power.UserOrgDO;
import com.iwindplus.mgt.server.dal.repository.power.UserOrgRepository;
import com.iwindplus.mgt.server.service.power.UserOrgService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户组织关系业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_USER_ORG})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class UserOrgServiceImpl implements UserOrgService {

    private final UserOrgRepository userOrgRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean save(UserOrgDTO entity) {
        List<UserOrgDO> entities = new ArrayList<>(10);
        UserOrgDO model = BeanUtil.copyProperties(entity, UserOrgDO.class);
        this.buildParam(model, entities);
        if (CollUtil.isNotEmpty(entities)) {
            this.userOrgRepository.saveBatch(entities, Constants.DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean saveBatchUser(Long orgId, Set<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Boolean.FALSE;
        }
        List<UserOrgDO> entities = new ArrayList<>(10);
        userIds.stream().filter(Objects::nonNull).forEach(userId -> {
            UserOrgDO entity = UserOrgDO.builder()
                .userId(userId)
                .orgId(orgId)
                .build();
            this.buildParam(entity, entities);
        });
        if (CollUtil.isNotEmpty(entities)) {
            this.userOrgRepository.saveBatch(entities, Constants.DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean saveBatchOrg(Long userId, Set<Long> orgIds) {
        if (CollUtil.isEmpty(orgIds)) {
            return Boolean.FALSE;
        }
        List<UserOrgDO> entities = new ArrayList<>(10);
        orgIds.stream().filter(Objects::nonNull).forEach(orgId -> {
            UserOrgDO entity = UserOrgDO.builder()
                .userId(userId)
                .orgId(orgId)
                .build();
            this.buildParam(entity, entities);
        });
        if (CollUtil.isNotEmpty(entities)) {
            this.userOrgRepository.saveBatch(entities, Constants.DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean saveBatch(List<UserOrgDTO> entities) {
        if (CollUtil.isEmpty(entities)) {
            return Boolean.FALSE;
        }
        List<UserOrgDO> params = new ArrayList<>(10);
        entities.stream().filter(Objects::nonNull).forEach(entity -> {
            UserOrgDO model = BeanUtil.copyProperties(entity, UserOrgDO.class);
            this.buildParam(model, params);
        });
        if (CollUtil.isNotEmpty(params)) {
            this.userOrgRepository.saveBatch(params, Constants.DEFAULT_BATCH_SIZE);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean removeByIds(List<Long> ids) {
        return CollUtil.isNotEmpty(ids) && SqlHelper.retBool(this.userOrgRepository.getBaseMapper().deleteByIds(ids));
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean removeByUserIds(List<Long> userIds) {
        return CollUtil.isNotEmpty(userIds) && SqlHelper.retBool(this.userOrgRepository.getBaseMapper().deleteByUserIds(userIds));
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean removeByOrgIds(List<Long> orgIds) {
        return CollUtil.isNotEmpty(orgIds) && SqlHelper.retBool(this.userOrgRepository.getBaseMapper().deleteByOrgIds(orgIds));
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean editBatchUser(Long orgId, Set<Long> userIds) {
        this.removeByOrgIds(Arrays.asList(orgId));
        this.saveBatchUser(orgId, userIds);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean editBatchOrg(Long userId, Set<Long> orgIds) {
        this.removeByUserIds(Arrays.asList(userId));
        this.saveBatchOrg(userId, orgIds);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_DEPARTMENT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_USER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_ROLE}, allEntries = true),
        }
    )
    @Override
    public boolean editChecked(Long newUserOrgId, Long oldUserOrgId) {
        List<Long> ids = Lists.newArrayList(newUserOrgId, oldUserOrgId);
        List<UserOrgDO> list = this.userOrgRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        Map<Long, Integer> idVersionMap = new HashMap<>(16);
        this.buildIdVersionMap(idVersionMap, list, ids);
        // 新组织
        UserOrgDO newUserOrg = new UserOrgDO();
        newUserOrg.setId(newUserOrgId);
        newUserOrg.setChecked(true);
        newUserOrg.setVersion(idVersionMap.get(newUserOrgId));
        this.userOrgRepository.updateById(newUserOrg);
        // 旧组织
        UserOrgDO oldUserOrg = new UserOrgDO();
        oldUserOrg.setId(oldUserOrgId);
        oldUserOrg.setChecked(false);
        oldUserOrg.setVersion(idVersionMap.get(oldUserOrgId));
        this.userOrgRepository.updateById(oldUserOrg);
        return Boolean.TRUE;
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null && #p1 != null", unless = "#result == null")
    @Override
    public boolean checkChangeOrg(Long userId, Long orgId) {
        final long count = this.userOrgRepository.count(Wrappers.lambdaQuery(UserOrgDO
            .builder()
            .userId(userId)
            .orgId(orgId)
            .checked(Boolean.TRUE)
            .build()));
        return SqlHelper.retBool(count);
    }

    private void buildParam(UserOrgDO entity, List<UserOrgDO> entities) {
        long count = this.userOrgRepository.count(Wrappers.lambdaQuery(UserOrgDO.class)
            .eq(UserOrgDO::getUserId, entity.getUserId())
            .eq(UserOrgDO::getOrgId, entity.getOrgId()));
        if (!SqlHelper.retBool(count)) {
            entity.setChecked(!this.checkIsChecked(entity.getUserId()));
            entities.add(entity);
        }
    }

    private boolean checkIsChecked(Long userId) {
        long count = this.userOrgRepository.count(Wrappers.lambdaQuery(UserOrgDO.class)
            .eq(UserOrgDO::getUserId, userId)
            .eq(UserOrgDO::getChecked, true));
        return SqlHelper.retBool(count);
    }

    private void buildIdVersionMap(Map<Long, Integer> idVersionMap, List<UserOrgDO> oldList, List<Long> idList) {
        oldList.forEach(oldData -> idList.forEach(newData -> {
            if (oldData.getId().equals(newData)) {
                idVersionMap.put(oldData.getId(), oldData.getVersion());
            }
        }));
    }
}
