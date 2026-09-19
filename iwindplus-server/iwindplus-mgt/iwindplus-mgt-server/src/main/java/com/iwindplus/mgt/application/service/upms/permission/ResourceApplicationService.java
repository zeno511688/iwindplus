/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.upms.permission;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.executor.RedissonExecutor;
import com.iwindplus.mgt.application.service.upms.permission.dto.ResourceDTO;
import com.iwindplus.mgt.application.service.upms.permission.dto.ResourceEditDTO;
import com.iwindplus.mgt.application.service.upms.permission.dto.ResourceSaveDTO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.ResourceDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.ResourceRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.permission.RoleResourceRepository;
import com.iwindplus.mgt.common.enums.MgtCodeEnum;
import com.iwindplus.mgt.common.enums.ResourceTypeEnum;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
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
public class ResourceApplicationService {

    private final RoleResourceRepository roleResourceRepository;
    private final ResourceRepository resourceRepository;
    private final RedissonExecutor redissonExecutor;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
        }
    )
    public boolean save(ResourceSaveDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setBuildInFlag(Boolean.FALSE);
        this.resourceRepository.getNameIsExist(entity.getName().trim(), entity.getMenuId());
        entity.setSeq(this.resourceRepository.getNextSeq(entity.getMenuId()));
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            final String key = entity.getResourceType().name().toLowerCase() + SymbolConstant.UNDERLINE;
            entity.setCode(this.redissonExecutor.serialNum().getSerialNumDate(key));
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
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
        }
    )
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
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
        }
    )
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
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
        }
    )
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
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
        }
    )
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

}
