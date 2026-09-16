/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.system.security;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.application.query.system.security.dto.IpBlackListSearchDTO;
import com.iwindplus.mgt.application.query.system.security.vo.IpBlackListPageVO;
import com.iwindplus.mgt.application.query.system.security.vo.IpBlackListVO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.system.security.IpBlackListDO;
import com.iwindplus.mgt.infrastructure.persistence.system.security.IpBlackListRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * IP黑名单查询业务层.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_IP_BLACK_LIST})
@RequiredArgsConstructor
public class IpBlackListQueryService {

    private final IpBlackListRepository ipBlackListRepository;

    public IPage<IpBlackListPageVO> page(IpBlackListSearchDTO entity) {
        PageDTO<IpBlackListDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<IpBlackListDO> queryWrapper = Wrappers.lambdaQuery(IpBlackListDO.class)
            .orderByDesc(IpBlackListDO::getModifiedTimestamp);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(IpBlackListDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getIp())) {
            queryWrapper.like(IpBlackListDO::getIp, entity.getIp().trim());
        }
        final PageDTO<IpBlackListDO> modelPage = this.ipBlackListRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, IpBlackListPageVO.class));
    }

    @Cacheable(key = "#root.methodName", unless = "#result == null")
    public List<String> listIp() {
        LambdaQueryWrapper<IpBlackListDO> queryWrapper = Wrappers.lambdaQuery(IpBlackListDO.class)
            .eq(IpBlackListDO::getStatus, EnableStatusEnum.ENABLE)
            .select(IpBlackListDO::getIp)
            .orderByAsc(List.of(IpBlackListDO::getIp));
        List<IpBlackListDO> list = this.ipBlackListRepository.list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return list.stream().filter(Objects::nonNull).map(IpBlackListDO::getIp).distinct().collect(Collectors.toCollection(ArrayList::new));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public IpBlackListVO getDetail(Long id) {
        IpBlackListDO data = this.ipBlackListRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, IpBlackListVO.class);
    }
}
