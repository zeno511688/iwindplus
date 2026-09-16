/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.system.app;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.application.query.system.app.dto.ClientSearchDTO;
import com.iwindplus.mgt.application.query.system.app.vo.ClientPageVO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.system.app.ClientDO;
import com.iwindplus.mgt.infrastructure.persistence.system.app.ClientRepository;
import com.iwindplus.mgt.api.system.vo.ClientVO;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 客户端查询业务层.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_CLIENT})
@RequiredArgsConstructor
public class ClientQueryService {

    private final ClientRepository clientRepository;

    public IPage<ClientPageVO> page(ClientSearchDTO entity) {
        PageDTO<ClientDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<ClientDO> queryWrapper = Wrappers.lambdaQuery(ClientDO.class)
            .orderByDesc(ClientDO::getModifiedTimestamp);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(ClientDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getClientId())) {
            queryWrapper.eq(ClientDO::getClientId, entity.getClientId().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getClientName())) {
            queryWrapper.eq(ClientDO::getClientName, entity.getClientName().trim());
        }
        queryWrapper.select(ClientDO::getId, ClientDO::getCreatedBy, ClientDO::getCreatedTimestamp, ClientDO::getModifiedTimestamp,
            ClientDO::getModifiedBy, ClientDO::getVersion, ClientDO::getStatus, ClientDO::getClientId, ClientDO::getClientName,
            ClientDO::getClientIdIssuedAt, ClientDO::getClientSecretExpiresAt);
        final PageDTO<ClientDO> modelPage = this.clientRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, ClientPageVO.class));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
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
