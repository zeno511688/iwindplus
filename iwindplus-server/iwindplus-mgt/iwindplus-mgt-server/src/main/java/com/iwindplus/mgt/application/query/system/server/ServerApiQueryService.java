/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.query.system.server;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.application.query.system.server.dto.ServerApiSearchDTO;
import com.iwindplus.mgt.application.query.system.server.vo.ServerApiGroupVO;
import com.iwindplus.mgt.application.query.system.server.vo.ServerApiGroupVO.ApiVO;
import com.iwindplus.mgt.application.query.system.server.vo.ServerApiPageVO;
import com.iwindplus.mgt.application.query.system.server.vo.ServerApiVO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.system.server.ServerApiDO;
import com.iwindplus.mgt.infrastructure.persistence.system.server.ServerApiRepository;
import com.iwindplus.mgt.api.system.vo.ServerApiBaseVO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 服务API查询业务层.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_SERVER_API})
@RequiredArgsConstructor
public class ServerApiQueryService {

    private final ServerApiRepository serverApiRepository;

    public IPage<ServerApiPageVO> page(ServerApiSearchDTO entity) {
        PageDTO<ServerApiDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<ServerApiDO> queryWrapper = Wrappers.lambdaQuery(ServerApiDO.class)
            .orderByDesc(ServerApiDO::getModifiedTimestamp);
        if (CharSequenceUtil.isNotBlank(entity.getAppName())) {
            queryWrapper.eq(ServerApiDO::getAppName, entity.getAppName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getControllerName())) {
            queryWrapper.like(ServerApiDO::getControllerName, entity.getControllerName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getApiName())) {
            queryWrapper.like(ServerApiDO::getApiName, entity.getApiName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getApiUrl())) {
            queryWrapper.like(ServerApiDO::getApiUrl, entity.getApiUrl().trim());
        }
        final PageDTO<ServerApiDO> modelPage = this.serverApiRepository.page(page, queryWrapper);
        return modelPage.convert(model -> BeanUtil.copyProperties(model, ServerApiPageVO.class));
    }

    @Cacheable(key = "#root.methodName", unless = "#result == null")
    public List<ServerApiGroupVO> listApiGroup() {
        final List<ServerApiDO> list = this.serverApiRepository.list();
        if (CollUtil.isEmpty(list)) {
            return null;
        }

        return list.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(ServerApiDO::getControllerName, LinkedHashMap::new, Collectors.toList()))
            .entrySet().stream()
            .filter(Objects::nonNull)
            .map(e -> ServerApiGroupVO.builder()
                .controllerName(e.getKey())
                .apis(this.listApi(e.getValue()))
                .build())
            .collect(Collectors.toList());
    }

    @Cacheable(key = "#root.methodName", unless = "#result == null")
    public List<ServerApiBaseVO> listApi() {
        final List<ServerApiDO> list = this.serverApiRepository.list();
        return this.buildServerApiBaseVO(list);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    public ServerApiVO getDetail(Long id) {
        ServerApiDO data = this.serverApiRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, ServerApiVO.class);
    }

    private List<ApiVO> listApi(List<ServerApiDO> serverApiList) {
        return serverApiList.stream()
            .map(serverApi ->
                ApiVO.builder()
                    .id(serverApi.getId())
                    .requestMethod(serverApi.getRequestMethod())
                    .apiName(serverApi.getApiName())
                    .apiUrl(serverApi.getApiUrl())
                    .build())
            .sorted(Comparator.comparing(ApiVO::getApiName)
                .thenComparing(ApiVO::getRequestMethod)
                .thenComparing(ApiVO::getApiUrl))
            .collect(Collectors.toList());
    }

    private List<ServerApiBaseVO> buildServerApiBaseVO(List<ServerApiDO> list) {
        return Optional.ofNullable(list)
            .orElse(Collections.emptyList())
            .stream()
            .map(serverApi -> ServerApiBaseVO.builder()
                .requestMethod(serverApi.getRequestMethod())
                .apiUrl(serverApi.getApiUrl())
                .rate(serverApi.getRate())
                .build())
            // 去重：根据 requestMethod + apiUrl 组合
            .collect(Collectors.collectingAndThen(
                Collectors.toMap(
                    vo -> vo.getRequestMethod() + "|" + vo.getApiUrl(),
                    Function.identity(),
                    (existing, replacement) -> existing,
                    LinkedHashMap::new
                ),
                map -> new ArrayList<>(map.values())
            ))
            .stream()
            .sorted(Comparator.comparing(ServerApiBaseVO::getRequestMethod)
                .thenComparing(ServerApiBaseVO::getApiUrl))
            .collect(Collectors.toCollection(ArrayList::new));
    }
}
