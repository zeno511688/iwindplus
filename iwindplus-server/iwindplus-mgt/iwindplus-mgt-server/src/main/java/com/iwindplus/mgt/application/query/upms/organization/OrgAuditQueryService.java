/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */
package com.iwindplus.mgt.application.query.upms.organization;

import com.iwindplus.mgt.application.query.upms.organization.vo.OrgAuditVO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.upms.organization.OrgAuditRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 组织审核查询业务层.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_ORG_AUDIT})
@RequiredArgsConstructor
public class OrgAuditQueryService {

    private final OrgAuditRepository orgAuditRepository;

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public List<OrgAuditVO> listByOrgId(Long orgId) {
        return this.orgAuditRepository.getBaseMapper().selectListByOrgId(orgId);
    }
}
