/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.upms.permission;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.mgt.application.query.upms.permission.vo.ResourceExtendVO;
import com.iwindplus.mgt.application.query.upms.permission.vo.ResourcePageVO;
import com.iwindplus.mgt.application.service.upms.permission.dto.ResourceSearchDTO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.ResourceDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.ResourceRepository;
import com.iwindplus.mgt.api.upms.vo.ResourceBaseExtendVO;
import com.iwindplus.mgt.api.upms.vo.ResourceBaseVO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 资源查询业务层.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_RESOURCE})
@RequiredArgsConstructor
public class ResourceQueryService {

    private final ResourceRepository resourceRepository;

    public IPage<ResourcePageVO> page(ResourceSearchDTO entity) {
        PageDTO<ResourceDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<ResourceDO> queryWrapper = Wrappers.lambdaQuery(ResourceDO.class)
            .eq(ResourceDO::getMenuId, entity.getMenuId())
            .orderByDesc(ResourceDO::getModifiedTimestamp);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(ResourceDO::getStatus, entity.getStatus());
        }
        if (Objects.nonNull(entity.getResourceType())) {
            queryWrapper.eq(ResourceDO::getResourceType, entity.getResourceType());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(ResourceDO::getCode, entity.getCode().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.like(ResourceDO::getName, entity.getName().trim());
        }
        queryWrapper.select(ResourceDO::getId, ResourceDO::getCreatedTimestamp, ResourceDO::getCreatedBy,
            ResourceDO::getModifiedTimestamp, ResourceDO::getModifiedBy, ResourceDO::getVersion, ResourceDO::getStatus,
            ResourceDO::getCode, ResourceDO::getName, ResourceDO::getBuildInFlag, ResourceDO::getResourceType, ResourceDO::getRequestMethod,
            ResourceDO::getApiUrl, ResourceDO::getSeq, ResourceDO::getMenuId
        );
        final PageDTO<ResourceDO> modelPage = this.resourceRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, ResourcePageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null && #p1 != null", unless = "#result == null")
    public List<ResourceBaseVO> listButtonCheckedByUserId(Long orgId, Long userId) {
        return this.resourceRepository.listButtonCheckedByUserId(orgId, userId);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null && #p1 != null", unless = "#result == null")
    public List<ResourceBaseExtendVO> listApiCheckedByUserId(Long orgId, Long userId) {
        return this.resourceRepository.listCheckedByUserId(orgId, userId, null, null, null);
    }

    @Cacheable(key = "#root.methodName", unless = "#result == null")
    public List<ResourceBaseExtendVO> listAll() {
        final List<ResourceDO> list = this.resourceRepository.listAll();
        return this.buildResourceBaseExtendVO(list);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1 + '_' + #p2 + '_' + #p3",
        condition = "#p0 != null && #p1 != null && #p2 != null && #p3 != null", unless = "#result == null")
    public Boolean checkApiByUserId(Long orgId, Long userId, String requestMethod, String apiUrl) {
        final List<ResourceBaseExtendVO> list = this.resourceRepository.listCheckedByUserId(orgId, userId, null, requestMethod, apiUrl);
        return CollUtil.isNotEmpty(list);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public ResourceExtendVO getDetailExtend(Long id) {
        return this.resourceRepository.getBaseMapper().selectDetailById(id);
    }

    private List<ResourceBaseExtendVO> buildResourceBaseExtendVO(List<ResourceDO> list) {
        return Optional.ofNullable(list).orElse(Collections.emptyList())
            .stream()
            .map(m -> ResourceBaseExtendVO.builder()
                .id(m.getId())
                .code(m.getCode())
                .name(m.getName())
                .requestMethod(m.getRequestMethod())
                .apiUrl(m.getApiUrl())
                .build())
            .sorted(Comparator.comparing(ResourceBaseVO::getName))
            .collect(Collectors.toCollection(ArrayList::new));
    }

}
