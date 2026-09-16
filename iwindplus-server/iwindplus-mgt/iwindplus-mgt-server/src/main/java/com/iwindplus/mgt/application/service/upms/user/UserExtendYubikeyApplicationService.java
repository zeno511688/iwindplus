/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.upms.user;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.application.service.upms.user.dto.UserExtendYubikeyEditDTO;
import com.iwindplus.mgt.application.service.upms.user.dto.UserExtendYubikeySaveDTO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserExtendYubikeyDO;
import com.iwindplus.mgt.infrastructure.persistence.upms.user.UserExtendYubikeyRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户扩展yubikey业务层实现类.
 *
 * @author zengdegui
 * @since 2026/04/26 20:26
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_USER_EXTEND_YUBIKEY})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class UserExtendYubikeyApplicationService {

    private final UserExtendYubikeyRepository userYubikeyRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_EXTEND_YUBIKEY}, allEntries = true),
        }
    )
    public boolean save(UserExtendYubikeySaveDTO entity) {
        this.userYubikeyRepository.getYubikeyIsExist(entity.getUserId(), entity.getBizType());
        final UserExtendYubikeyDO model = BeanUtil.copyProperties(entity, UserExtendYubikeyDO.class);
        this.userYubikeyRepository.save(model);
        entity.setId(model.getId());
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_EXTEND_YUBIKEY}, allEntries = true),
        }
    )
    public boolean removeByIds(List<Long> ids) {
        List<UserExtendYubikeyDO> list = this.userYubikeyRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.userYubikeyRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_USER_EXTEND_YUBIKEY}, allEntries = true),
        }
    )
    public boolean edit(UserExtendYubikeyEditDTO entity) {
        UserExtendYubikeyDO data = this.userYubikeyRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final UserExtendYubikeyDO model = BeanUtil.copyProperties(entity, UserExtendYubikeyDO.class);
        this.userYubikeyRepository.updateById(model);
        return Boolean.TRUE;
    }

}
