/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */
package com.iwindplus.mgt.server.service.power.impl;

import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.power.OrgAuditDTO;
import com.iwindplus.mgt.domain.vo.power.OrgAuditVO;
import com.iwindplus.mgt.server.dal.repository.power.OrgAuditRepository;
import com.iwindplus.mgt.server.service.power.OrgAuditService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 组织审核业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_ORG_AUDIT})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class OrgAuditServiceImpl implements OrgAuditService {

    private final OrgAuditRepository orgAuditRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG_AUDIT}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ORG}, allEntries = true)
        }
    )
    @Override
    public boolean save(OrgAuditDTO entity) {
        return this.orgAuditRepository.save(entity);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public List<OrgAuditVO> listByOrgId(Long orgId) {
        return this.orgAuditRepository.getBaseMapper().selectListByOrgId(orgId);
    }

}
