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
import com.iwindplus.mgt.application.query.system.security.dto.ApiWhiteListSearchDTO;
import com.iwindplus.mgt.application.query.system.security.vo.ApiWhiteListPageVO;
import com.iwindplus.mgt.application.query.system.security.vo.ApiWhiteListVO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.system.security.ApiWhiteListDO;
import com.iwindplus.mgt.infrastructure.persistence.system.security.ApiWhiteListRepository;
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
 * API白名单查询业务层.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_API_WHITE_LIST})
@Slf4j
@RequiredArgsConstructor
public class ApiWhiteListQueryService {

    private final ApiWhiteListRepository apiWhiteListRepository;

    public IPage<ApiWhiteListPageVO> page(ApiWhiteListSearchDTO entity) {
        PageDTO<ApiWhiteListDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<ApiWhiteListDO> queryWrapper = Wrappers.lambdaQuery(ApiWhiteListDO.class)
            .orderByDesc(ApiWhiteListDO::getModifiedTimestamp);
        if (Objects.nonNull(entity.getStatus())) {
            queryWrapper.eq(ApiWhiteListDO::getStatus, entity.getStatus());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode())) {
            queryWrapper.eq(ApiWhiteListDO::getCode, entity.getCode().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getName())) {
            queryWrapper.eq(ApiWhiteListDO::getName, entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getApiUrl())) {
            queryWrapper.like(ApiWhiteListDO::getApiUrl, entity.getApiUrl().trim());
        }
        final PageDTO<ApiWhiteListDO> modelPage = this.apiWhiteListRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, ApiWhiteListPageVO.class));
    }

    @Cacheable(key = "#root.methodName", unless = "#result == null")
    public List<String> listApi() {
        LambdaQueryWrapper<ApiWhiteListDO> queryWrapper = Wrappers.lambdaQuery(ApiWhiteListDO.class)
            .eq(ApiWhiteListDO::getStatus, EnableStatusEnum.ENABLE)
            .select(ApiWhiteListDO::getApiUrl)
            .orderByAsc(List.of(ApiWhiteListDO::getApiUrl));
        List<ApiWhiteListDO> list = this.apiWhiteListRepository.list(queryWrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return list.stream().filter(Objects::nonNull).map(ApiWhiteListDO::getApiUrl).distinct().collect(Collectors.toCollection(ArrayList::new));
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public ApiWhiteListVO getDetail(Long id) {
        ApiWhiteListDO data = this.apiWhiteListRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, ApiWhiteListVO.class);
    }

}
