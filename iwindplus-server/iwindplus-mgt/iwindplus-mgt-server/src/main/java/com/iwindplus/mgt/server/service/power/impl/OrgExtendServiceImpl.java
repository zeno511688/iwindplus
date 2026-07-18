/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power.impl;

import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.power.OrgExtendDTO;
import com.iwindplus.mgt.server.dal.repository.power.OrgExtendRepository;
import com.iwindplus.mgt.server.service.power.OrgExtendService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 组织扩展业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_ORG_EXTEND})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class OrgExtendServiceImpl implements OrgExtendService {

    private final OrgExtendRepository orgExtendRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG_EXTEND}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true)
        }
    )
    @Override
    public boolean save(OrgExtendDTO entity) {
        return this.orgExtendRepository.save(entity);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG_EXTEND}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true)
        }
    )
    @Override
    public boolean edit(OrgExtendDTO entity) {
        return this.orgExtendRepository.edit(entity);
    }
}
