/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.upms.user;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.executor.RedissonExecutor;
import com.iwindplus.mgt.application.service.upms.user.dto.UserGroupSaveEditDTO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserGroupDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserGroupRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserGroupRoleRepository;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserGroupUserRepository;
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
 * 组业务层接口实现类.
 *
 * @author zengdegui
 * @since 2019/10/9
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class UserGroupApplicationService {

    private final RedissonExecutor redissonExecutor;
    private final UserGroupRepository userGroupRepository;
    private final UserGroupUserRepository userGroupUserRepository;
    private final UserGroupRoleRepository userGroupRoleRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    public boolean save(UserGroupSaveEditDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        entity.setBuildInFlag(Boolean.FALSE);
        this.userGroupRepository.getNameIsExist(entity.getName().trim(), entity.getOrgId());
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            entity.setCode(this.redissonExecutor.serialNum().getSerialNumDate(MgtCodePrefixEnum.USER_GROUP_PREFIX.getValue()));
        }
        this.userGroupRepository.getCodeIsExist(entity.getCode().trim(), entity.getOrgId());
        entity.setSeq(this.userGroupRepository.getNextSeq(entity.getOrgId()));
        final UserGroupDO model = BeanUtil.copyProperties(entity, UserGroupDO.class);
        this.userGroupRepository.save(model);
        this.userGroupRoleRepository.saveBatchRole(model.getId(), entity.getRoleIds());
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    public boolean removeByIds(List<Long> ids) {
        List<UserGroupDO> list = this.userGroupRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().filter(Objects::nonNull).anyMatch(UserGroupDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.userGroupUserRepository.getBaseMapper().deleteByUserGroupIds(ids);
        this.userGroupRoleRepository.getBaseMapper().deleteByUserGroupIds(ids);
        this.userGroupRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    public boolean edit(UserGroupSaveEditDTO entity) {
        // 编辑
        UserGroupDO data = this.userGroupRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        // 校验名称是否存在
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.userGroupRepository.getNameIsExist(entity.getName().trim(), data.getOrgId());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.userGroupRepository.getCodeIsExist(entity.getCode().trim(), data.getOrgId());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final UserGroupDO model = BeanUtil.copyProperties(entity, UserGroupDO.class);
        this.userGroupRepository.updateById(model);
        this.userGroupRoleRepository.editBatchRole(model.getId(), entity.getRoleIds());
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    public boolean editStatus(Long id, EnableStatusEnum status) {
        UserGroupDO data = this.userGroupRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        UserGroupDO param = new UserGroupDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.userGroupRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_GROUP_ROLE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_RESOURCE}, allEntries = true),
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_ROLE}, allEntries = true),
        }
    )
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        UserGroupDO data = this.userGroupRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        UserGroupDO param = new UserGroupDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.userGroupRepository.updateById(param);
        return Boolean.TRUE;
    }

}
