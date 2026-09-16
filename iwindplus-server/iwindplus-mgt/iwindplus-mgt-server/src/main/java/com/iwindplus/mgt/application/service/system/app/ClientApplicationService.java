/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.system.app;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.TimeToLiveUnitEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.util.SecureRandomUtil;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.api.system.dto.ClientDTO;
import com.iwindplus.mgt.api.system.dto.ClientSettingDTO;
import com.iwindplus.mgt.api.system.dto.TokenSettingDTO;
import com.iwindplus.mgt.api.system.vo.ClientBaseVO;
import com.iwindplus.mgt.infrastructure.persistence.system.app.ClientDO;
import com.iwindplus.mgt.infrastructure.persistence.system.app.ClientRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 客户端业务层接口实现类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_CLIENT})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class ClientApplicationService {

    private final PasswordEncoder passwordEncoder;
    private final ClientRepository clientRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_CLIENT}, allEntries = true),
        }
    )
    public ClientBaseVO save(ClientDTO entity) {
        if (CollUtil.isEmpty(entity.getAuthenticationMethod())) {
            entity.setAuthenticationMethod(ClientRepository.DEFAULT_AUTHENTICATION_METHOD);
        }
        if (CollUtil.isEmpty(entity.getScope())) {
            entity.setScope(ClientRepository.DEFAULT_SCOPE);
        }
        if (Objects.isNull(entity.getClientSetting())) {
            ClientSettingDTO clientSetting = ClientSettingDTO
                .builder()
                .requireAuthorizationConsent(Boolean.TRUE)
                .build();
            entity.setClientSetting(clientSetting);
        }
        if (Objects.isNull(entity.getTokenSetting())) {
            TokenSettingDTO tokenSetting = TokenSettingDTO
                .builder()
                .accessTokenFormat(ClientRepository.DEFAULT_ACCESS_TOKEN_FORMAT)
                .authorizationCodeTimeToLive((long) NumberConstant.NUMBER_FIVE)
                .authorizationCodeTimeToLiveUnit(TimeToLiveUnitEnum.MINUTES)
                .accessTokenTimeToLive((long) NumberConstant.NUMBER_TWO)
                .accessTokenTimeToLiveUnit(TimeToLiveUnitEnum.HOURS)
                .deviceCodeTimeToLive((long) NumberConstant.NUMBER_FIVE)
                .deviceCodeTimeToLiveUnit(TimeToLiveUnitEnum.MINUTES)
                .reuseRefreshTokens(Boolean.FALSE)
                .refreshTokenTimeToLive((long) NumberConstant.NUMBER_SEVEN)
                .refreshTokenTimeToLiveUnit(TimeToLiveUnitEnum.DAYS)
                .build();
            entity.setTokenSetting(tokenSetting);
        }
        String clientId = IdUtil.simpleUUID();
        String secret = SecureRandomUtil.randomString(32);
        final ClientDO model = BeanUtil.copyProperties(entity, ClientDO.class);
        model.setClientId(clientId);
        model.setClientIdIssuedAt(System.currentTimeMillis());
        if (Objects.isNull(model.getClientSecretExpiresAt())) {
            model.setClientSecretExpiresAt(System.currentTimeMillis() + 100 * 365 * 24 * 60 * 60 * 1000L);
        }
        model.setClientSecret(this.passwordEncoder.encode(secret));
        this.clientRepository.save(model);
        entity.setId(model.getId());
        return ClientBaseVO.builder()
            .id(model.getId())
            .clientId(model.getClientId())
            .clientName(model.getClientName())
            .clientSecret(model.getClientSecret())
            .build();
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_CLIENT}, allEntries = true),
        }
    )
    public boolean removeByIds(List<Long> ids) {
        List<ClientDO> list = this.clientRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.clientRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_CLIENT}, allEntries = true),
        }
    )
    public boolean edit(ClientDTO entity) {
        final ClientDO data = this.clientRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        final ClientDO model = BeanUtil.copyProperties(entity, ClientDO.class);
        this.clientRepository.updateById(model);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_CLIENT}, allEntries = true),
        }
    )
    public boolean editStatus(Long id, EnableStatusEnum status) {
        ClientDO data = this.clientRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        ClientDO param = new ClientDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.clientRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_CLIENT}, allEntries = true),
        }
    )
    public ClientBaseVO editSecret(Long id) {
        final ClientDO data = this.clientRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        // 生成新的密钥
        String secret = SecureRandomUtil.randomString(32);
        ClientDO param = new ClientDO();
        param.setId(data.getId());
        param.setClientSecret(this.passwordEncoder.encode(secret));
        this.clientRepository.updateById(param);
        return ClientBaseVO.builder()
            .id(data.getId())
            .clientId(data.getClientId())
            .clientName(data.getClientName())
            .clientSecret(secret)
            .build();
    }
}
