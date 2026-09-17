/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.system.app;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.api.system.vo.ClientVO;
import com.iwindplus.mgt.application.query.system.app.dto.ClientSearchDTO;
import com.iwindplus.mgt.application.query.system.app.vo.ClientPageVO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.system.app.ClientDO;
import com.iwindplus.mgt.infrastructure.persistence.system.app.ClientRepository;
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

    /**
     * 分页查询.
     *
     * @param entity 查询参数
     * @return 分页查询结果
     */
    public IPage<ClientPageVO> page(ClientSearchDTO entity) {
        return this.clientRepository.page(entity);
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
