/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.upms.organization;

import com.iwindplus.mgt.application.service.upms.organization.dto.OrgExtendDTO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.upms.organization.OrgExtendRepository;
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
public class OrgExtendApplicationService {

    private final OrgExtendRepository orgExtendRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG_EXTEND}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true)
        }
    )
    public boolean save(OrgExtendDTO entity) {
        return this.orgExtendRepository.save(entity);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG_EXTEND}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true)
        }
    )
    public boolean edit(OrgExtendDTO entity) {
        return this.orgExtendRepository.edit(entity);
    }
}
