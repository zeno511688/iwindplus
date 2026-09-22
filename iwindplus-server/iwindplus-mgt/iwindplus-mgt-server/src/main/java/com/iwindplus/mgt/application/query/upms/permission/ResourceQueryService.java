/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.upms.permission;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.mgt.api.upms.vo.ResourceBaseExtendVO;
import com.iwindplus.mgt.api.upms.vo.ResourceBaseVO;
import com.iwindplus.mgt.application.query.upms.permission.vo.ResourceExtendVO;
import com.iwindplus.mgt.application.query.upms.permission.vo.ResourcePageVO;
import com.iwindplus.mgt.application.service.upms.permission.dto.ResourceSearchDTO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.MenuRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.ResourceDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.ResourceRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
    private final MenuRepository menuRepository;

    /**
     * 分页查询.
     *
     * @param entity 查询参数
     * @return 分页查询结果
     */
    public IPage<ResourcePageVO> page(ResourceSearchDTO entity) {
        return this.resourceRepository.page(entity);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null && #p1 != null", unless = "#result == null")
    public List<ResourceBaseExtendVO> listApiCheckedByUserId(Long orgId, Long userId) {
        List<ResourceBaseExtendVO> resourceList = this.resourceRepository.getBaseMapper().selectListCheckedByUserId(orgId, userId, null);
        List<ResourceBaseExtendVO> menuList = this.menuRepository.getBaseMapper().selectListCheckedByUserId(orgId, userId, null);
        List<ResourceBaseExtendVO> result = new ArrayList<>(10);
        if (CollUtil.isNotEmpty(resourceList)) {
            result.addAll(resourceList);
        }
        if (CollUtil.isNotEmpty(menuList)) {
            result.addAll(menuList);
        }
        return result;
    }

    @Cacheable(key = "#root.methodName", unless = "#result == null")
    public List<ResourceBaseExtendVO> listAll() {
        final List<ResourceDO> list = this.resourceRepository.listAll();
        return this.buildResourceBaseExtendVO(list);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1 + '_' + #p2",
        condition = "#p0 != null && #p1 != null && #p2 != null", unless = "#result == null")
    public Boolean checkApiByUserId(Long orgId, Long userId, String path) {
        final List<ResourceBaseExtendVO> resourceList = this.resourceRepository.getBaseMapper().selectListCheckedByUserId(orgId, userId, path);
        if (CollUtil.isNotEmpty(resourceList)) {
            return Boolean.TRUE;
        }
        final List<ResourceBaseExtendVO> menuList = this.menuRepository.getBaseMapper().selectListCheckedByUserId(orgId, userId, path);
        return CollUtil.isNotEmpty(menuList);
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
                .apiUrls(m.getApiUrls())
                .build())
            .sorted(Comparator.comparing(ResourceBaseVO::getName))
            .collect(Collectors.toCollection(ArrayList::new));
    }

}
