/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.system.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.TimeToLiveUnitEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.system.ClientDTO;
import com.iwindplus.mgt.domain.dto.system.ClientSearchDTO;
import com.iwindplus.mgt.domain.dto.system.ClientSettingDTO;
import com.iwindplus.mgt.domain.dto.system.TokenSettingDTO;
import com.iwindplus.mgt.domain.vo.system.ClientBaseVO;
import com.iwindplus.mgt.domain.vo.system.ClientPageVO;
import com.iwindplus.mgt.domain.vo.system.ClientVO;
import com.iwindplus.mgt.server.dal.model.system.ClientDO;
import com.iwindplus.mgt.server.dal.repository.system.ClientRepository;
import com.iwindplus.mgt.server.service.system.ClientService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import net.dreamlu.mica.core.utils.StringUtil;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
public class ClientServiceImpl implements ClientService {

    private final PasswordEncoder passwordEncoder;
    private final ClientRepository clientRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_CLIENT}, allEntries = true),
        }
    )
    @Override
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
                .authorizationCodeTimeToLive(Long.valueOf(NumberConstant.NUMBER_FIVE))
                .authorizationCodeTimeToLiveUnit(TimeToLiveUnitEnum.MINUTES)
                .accessTokenTimeToLive(Long.valueOf(NumberConstant.NUMBER_TWO))
                .accessTokenTimeToLiveUnit(TimeToLiveUnitEnum.HOURS)
                .deviceCodeTimeToLive(Long.valueOf(NumberConstant.NUMBER_FIVE))
                .deviceCodeTimeToLiveUnit(TimeToLiveUnitEnum.MINUTES)
                .reuseRefreshTokens(Boolean.FALSE)
                .refreshTokenTimeToLive(Long.valueOf(NumberConstant.NUMBER_SEVEN))
                .refreshTokenTimeToLiveUnit(TimeToLiveUnitEnum.DAYS)
                .build();
            entity.setTokenSetting(tokenSetting);
        }
        String clientId = IdUtil.simpleUUID();
        String secret = RandomUtil.randomString(32);
        final ClientDO model = BeanUtil.copyProperties(entity, ClientDO.class);
        model.setClientId(clientId);
        model.setClientIdIssuedAt(LocalDateTime.now());
        if (Objects.isNull(model.getClientSecretExpiresAt())) {
            model.setClientSecretExpiresAt(LocalDateTime.now().plusYears(100));
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public ClientBaseVO editSecret(Long id) {
        final ClientDO data = this.clientRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        // 生成新的密钥
        String secret = RandomUtil.randomString(32);
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

    @Override
    public IPage<ClientPageVO> page(ClientSearchDTO entity) {
        PageDTO<ClientDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<ClientDO> queryWrapper = Wrappers.lambdaQuery(ClientDO.class)
            .orderByDesc(ClientDO::getModifiedTime);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(ClientDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getClientId())) {
            queryWrapper.eq(ClientDO::getClientId, entity.getClientId().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getClientName())) {
            queryWrapper.eq(ClientDO::getClientName, entity.getClientName().trim());
        }
        queryWrapper.select(ClientDO::getId, ClientDO::getCreatedTime, ClientDO::getCreatedBy, ClientDO::getModifiedTime,
            ClientDO::getModifiedBy, ClientDO::getVersion, ClientDO::getStatus, ClientDO::getClientId, ClientDO::getClientName,
            ClientDO::getClientIdIssuedAt, ClientDO::getClientSecretExpiresAt);
        final PageDTO<ClientDO> modelPage = this.clientRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, ClientPageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public ClientVO getByClientId(String clientId) {
        final ClientDO data = this.clientRepository.getOne(Wrappers.lambdaQuery(ClientDO.class)
            .eq(ClientDO::getClientId, clientId)
            .eq(ClientDO::getStatus, EnableStatusEnum.ENABLE));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, ClientVO.class);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public ClientVO getDetail(String id) {
        final ClientDO data = this.clientRepository.getOne(Wrappers.lambdaQuery(ClientDO.class)
            .eq(ClientDO::getId, id)
            .eq(ClientDO::getStatus, EnableStatusEnum.ENABLE));
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, ClientVO.class);
    }
}
