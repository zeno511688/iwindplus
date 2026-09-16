/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.system.app;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.executor.RedissonExecutor;
import com.iwindplus.integr.client.OssClient;
import com.iwindplus.mgt.application.service.system.app.dto.SystemEditDTO;
import com.iwindplus.mgt.application.service.system.app.dto.SystemSaveDTO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.configuration.MgtProperty;
import com.iwindplus.mgt.infrastructure.persistence.system.app.SystemDO;
import com.iwindplus.mgt.infrastructure.persistence.system.app.SystemRepository;
import com.iwindplus.mgt.common.enums.MgtCodePrefixEnum;
import com.iwindplus.mgt.application.service.upms.organization.OrgApplicationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_SYSTEM})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class SystemApplicationService {

    private final SystemRepository systemRepository;
    private final OssClient ossClient;
    private final RedissonExecutor redissonExecutor;
    private final MgtProperty property;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SYSTEM}, allEntries = true),
        }
    )
    public boolean save(SystemSaveDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setHideFlag(Boolean.FALSE);
        entity.setBuildInFlag(Boolean.FALSE);
        this.systemRepository.getNameIsExist(entity.getName().trim());
        entity.setSeq(this.systemRepository.getNextSeq());
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            entity.setCode(this.redissonExecutor.serialNum().getSerialNumDate(MgtCodePrefixEnum.SYSTEM_PREFIX.getValue()));
        }
        this.systemRepository.getCodeIsExist(entity.getCode().trim());
        final SystemDO model = BeanUtil.copyProperties(entity, SystemDO.class);
        this.systemRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SYSTEM}, allEntries = true),
        }
    )
    public boolean removeByIds(List<Long> ids) {
        List<SystemDO> list = this.systemRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().anyMatch(SystemDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.systemRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SYSTEM}, allEntries = true),
        }
    )
    public boolean edit(SystemEditDTO entity) {
        SystemDO data = this.systemRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            // 校验名称是否存在
            this.systemRepository.getNameIsExist(entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.systemRepository.getCodeIsExist(entity.getCode().trim());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final SystemDO model = BeanUtil.copyProperties(entity, SystemDO.class);
        this.systemRepository.updateById(model);
        this.removeOldPic(entity, data);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SYSTEM}, allEntries = true),
        }
    )
    public boolean editStatus(Long id, EnableStatusEnum status) {
        SystemDO data = this.systemRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        SystemDO param = new SystemDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.systemRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_MENU}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SYSTEM}, allEntries = true),
        }
    )
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        SystemDO data = this.systemRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        SystemDO param = new SystemDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.systemRepository.updateById(param);
        return Boolean.TRUE;
    }

    private void removeOldPic(SystemEditDTO entity, SystemDO data) {
        List<String> relativePaths = new ArrayList<>(10);
        if (CharSequenceUtil.isNotBlank(entity.getIconUrl())
            && CharSequenceUtil.isNotBlank(data.getIconUrl())
            && !CharSequenceUtil.equals(data.getIconUrl(), entity.getIconUrl().trim())) {
            relativePaths.add(data.getIconUrl());
        }
        OrgApplicationService.removeFiles(this.ossClient,
            this.property.getOss().getCode(),
            this.property.getOss().getTplCode(), relativePaths);
    }

}
