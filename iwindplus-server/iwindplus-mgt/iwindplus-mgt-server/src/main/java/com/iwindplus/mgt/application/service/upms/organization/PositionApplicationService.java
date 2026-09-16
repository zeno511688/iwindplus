/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.upms.organization;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.executor.RedissonExecutor;
import com.iwindplus.mgt.application.service.upms.organization.dto.PositionEditDTO;
import com.iwindplus.mgt.application.service.upms.organization.dto.PositionSaveDTO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.upms.organization.PositionDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.organization.PositionRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserPositionRepository;
import com.iwindplus.mgt.common.enums.MgtCodePrefixEnum;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 职位业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_POSITION})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class PositionApplicationService {

    private final RedissonExecutor redissonExecutor;
    private final PositionRepository positionRepository;
    private final UserPositionRepository userPositionRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    public boolean save(PositionSaveDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setBuildInFlag(Boolean.FALSE);
        this.positionRepository.getNameIsExist(entity.getName().trim(), entity.getDepartmentId(), entity.getOrgId());
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            entity.setCode(this.redissonExecutor.serialNum().getSerialNumDate(MgtCodePrefixEnum.POSITION_PREFIX.getValue()));
        }
        this.positionRepository.getCodeIsExist(entity.getCode().trim(), entity.getDepartmentId(), entity.getOrgId());
        entity.setSeq(this.positionRepository.getNextSeq(entity.getDepartmentId(), entity.getOrgId()));
        final PositionDO model = BeanUtil.copyProperties(entity, PositionDO.class);
        this.positionRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    public boolean removeByIds(List<Long> ids) {
        List<PositionDO> list = this.positionRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().filter(Objects::nonNull).anyMatch(PositionDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.userPositionRepository.getBaseMapper().deleteByPositionIds(ids);
        this.positionRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    public boolean edit(PositionEditDTO entity) {
        PositionDO data = this.positionRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        // 校验名称是否存在
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.positionRepository.getNameIsExist(entity.getName().trim(), data.getDepartmentId(), data.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.positionRepository.getCodeIsExist(entity.getCode().trim(), data.getDepartmentId(), data.getOrgId());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final PositionDO model = BeanUtil.copyProperties(entity, PositionDO.class);
        this.positionRepository.updateById(model);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    public boolean editStatus(Long id, EnableStatusEnum status) {
        PositionDO data = this.positionRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        PositionDO param = new PositionDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.positionRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_POSITION}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        PositionDO data = this.positionRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        PositionDO param = new PositionDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.positionRepository.updateById(param);
        return Boolean.TRUE;
    }

}
