/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.power.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.power.ResourceDTO;
import com.iwindplus.mgt.domain.dto.power.ResourceEditDTO;
import com.iwindplus.mgt.domain.dto.power.ResourceSaveDTO;
import com.iwindplus.mgt.domain.dto.power.ResourceSearchDTO;
import com.iwindplus.mgt.domain.enums.MgtCodeEnum;
import com.iwindplus.mgt.domain.enums.ResourceTypeEnum;
import com.iwindplus.mgt.domain.vo.power.ResourceBaseExtendVO;
import com.iwindplus.mgt.domain.vo.power.ResourceBaseVO;
import com.iwindplus.mgt.domain.vo.power.ResourceExtendVO;
import com.iwindplus.mgt.domain.vo.power.ResourcePageVO;
import com.iwindplus.mgt.server.dal.model.power.ResourceDO;
import com.iwindplus.mgt.server.dal.repository.power.ResourceRepository;
import com.iwindplus.mgt.server.dal.repository.power.RoleResourceRepository;
import com.iwindplus.mgt.server.service.power.ResourceService;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 资源业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_RESOURCE})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final RoleResourceRepository roleResourceRepository;
    private final ResourceRepository resourceRepository;
    private final RedissonService redissonService;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SERVER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
        }
    )
    @Override
    public boolean save(ResourceSaveDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setBuildInFlag(Boolean.FALSE);
        this.resourceRepository.getNameIsExist(entity.getName().trim(), entity.getMenuId());
        entity.setSeq(this.resourceRepository.getNextSeq(entity.getMenuId()));
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            final String key = new StringBuilder(entity.getResourceType().name().toLowerCase()).append(SymbolConstant.UNDERLINE).toString();
            entity.setCode(this.redissonService.serialNum().getSerialNumDate(key));
        } else {
            this.checkCode(entity);
            this.resourceRepository.getCodeIsExist(entity.getCode());
        }
        this.resourceRepository.getApiUrlIsExist(entity.getApiUrl().trim(), entity.getMenuId());
        final ResourceDO model = BeanUtil.copyProperties(entity, ResourceDO.class);
        this.resourceRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SERVER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
        }
    )
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<ResourceDO> list = this.resourceRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().anyMatch(ResourceDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.roleResourceRepository.getBaseMapper().deleteByResourceIds(ids);
        this.resourceRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SERVER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
        }
    )
    @Override
    public boolean edit(ResourceEditDTO entity) {
        ResourceDO data = this.resourceRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.resourceRepository.getNameIsExist(entity.getName().trim(), data.getMenuId());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.checkCode(entity);
            this.resourceRepository.getCodeIsExist(entity.getCode());
        }
        if (CharSequenceUtil.isNotBlank(entity.getApiUrl()) && !CharSequenceUtil.equals(data.getApiUrl(), entity.getApiUrl().trim())) {
            this.resourceRepository.getApiUrlIsExist(entity.getApiUrl().trim(), data.getMenuId());
        }
        final ResourceDO model = BeanUtil.copyProperties(entity, ResourceDO.class);
        this.resourceRepository.updateById(model);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SERVER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
        }
    )
    @Override
    public boolean editStatus(Long id, EnableStatusEnum status) {
        ResourceDO data = this.resourceRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        ResourceDO param = new ResourceDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.resourceRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SERVER}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
        }
    )
    @Override
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        ResourceDO data = this.resourceRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        ResourceDO param = new ResourceDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.resourceRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Override
    public IPage<ResourcePageVO> page(ResourceSearchDTO entity) {
        PageDTO<ResourceDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<ResourceDO> queryWrapper = Wrappers.lambdaQuery(ResourceDO.class)
            .eq(ResourceDO::getMenuId, entity.getMenuId())
            .orderByDesc(ResourceDO::getModifiedTime);
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
        queryWrapper.select(ResourceDO::getId, ResourceDO::getCreatedTime, ResourceDO::getCreatedTimestamp, ResourceDO::getCreatedBy,
            ResourceDO::getModifiedTime, ResourceDO::getModifiedTimestamp, ResourceDO::getModifiedBy, ResourceDO::getVersion, ResourceDO::getStatus,
            ResourceDO::getCode, ResourceDO::getName, ResourceDO::getBuildInFlag, ResourceDO::getResourceType, ResourceDO::getRequestMethod,
            ResourceDO::getApiUrl, ResourceDO::getSeq, ResourceDO::getMenuId
        );
        final PageDTO<ResourceDO> modelPage = this.resourceRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, ResourcePageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null && #p1 != null", unless = "#result == null")
    @Override
    public List<ResourceBaseVO> listButtonCheckedByUserId(Long orgId, Long userId) {
        return this.resourceRepository.listButtonCheckedByUserId(orgId, userId);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1", condition = "#p0 != null && #p1 != null", unless = "#result == null")
    @Override
    public List<ResourceBaseExtendVO> listApiCheckedByUserId(Long orgId, Long userId) {
        return this.resourceRepository.listCheckedByUserId(orgId, userId, null, null, null);
    }

    @Cacheable(key = "#root.methodName", unless = "#result == null")
    @Override
    public List<ResourceBaseExtendVO> listAll() {
        final List<ResourceDO> list = this.resourceRepository.listAll();
        return this.buildResourceBaseExtendVO(list);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0 + '_' + #p1 + '_' + #p2 + '_' + #p3",
        condition = "#p0 != null && #p1 != null && #p2 != null && #p3 != null", unless = "#result == null")
    @Override
    public Boolean checkApiByUserId(Long orgId, Long userId, String requestMethod, String apiUrl) {
        final List<ResourceBaseExtendVO> list = this.resourceRepository.listCheckedByUserId(orgId, userId, null, requestMethod, apiUrl);
        if (CollUtil.isEmpty(list)) {
            return false;
        }
        return true;
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public ResourceExtendVO getDetailExtend(Long id) {
        return this.resourceRepository.getBaseMapper().selectDetailById(id);
    }

    private void checkCode(ResourceDTO entity) {
        if (ResourceTypeEnum.BUTTON.equals(entity.getResourceType())) {
            if (!entity.getCode().startsWith(ResourceTypeEnum.BUTTON.name().toLowerCase())) {
                throw new BizException(MgtCodeEnum.BUTTON_PREFIX_ERROR);
            }
        } else if (ResourceTypeEnum.API.equals(entity.getResourceType())) {
            if (!entity.getCode().startsWith(ResourceTypeEnum.API.name().toLowerCase())) {
                throw new BizException(MgtCodeEnum.API_PREFIX_ERROR);
            }
        }
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
