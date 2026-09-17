/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.system.security;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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

    /**
     * 分页查询.
     *
     * @param entity 查询参数
     * @return 分页查询结果
     */
    public IPage<ApiWhiteListPageVO> page(ApiWhiteListSearchDTO entity) {
        return this.apiWhiteListRepository.page(entity);
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
