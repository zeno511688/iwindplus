/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.system.impl;

import static com.baomidou.mybatisplus.extension.repository.IRepository.DEFAULT_BATCH_SIZE;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.system.ServerApiDTO;
import com.iwindplus.mgt.domain.dto.system.ServerApiSearchDTO;
import com.iwindplus.mgt.domain.vo.system.ServerApiBaseVO;
import com.iwindplus.mgt.domain.vo.system.ServerApiGroupVO;
import com.iwindplus.mgt.domain.vo.system.ServerApiGroupVO.ApiVO;
import com.iwindplus.mgt.domain.vo.system.ServerApiPageVO;
import com.iwindplus.mgt.domain.vo.system.ServerApiVO;
import com.iwindplus.mgt.server.dal.model.system.ServerApiDO;
import com.iwindplus.mgt.server.dal.repository.system.ServerApiRepository;
import com.iwindplus.mgt.server.service.system.ServerApiService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务API业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Slf4j
@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_SERVER_API})
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class ServerApiServiceImpl implements ServerApiService {

    private final ServerApiRepository serverApiRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SERVER_API}, allEntries = true),
        }
    )
    @Override
    public boolean save(ServerApiDTO entity) {
        entity.setRate(2000L);
        entity.setHideFlag(Boolean.FALSE);
        this.serverApiRepository.getControllerNameIsExist(entity.getAppName().trim(), entity.getControllerName().trim());
        this.serverApiRepository.getApiNameIsExist(entity.getAppName().trim(), entity.getApiName().trim());
        this.serverApiRepository.getApiUrlIsExist(entity.getAppName().trim(), entity.getApiUrl().trim());
        entity.setSeq(this.serverApiRepository.getNextSeq());
        ServerApiDO model = BeanUtil.copyProperties(entity, ServerApiDO.class);
        boolean data = this.serverApiRepository.save(model);
        entity.setId(model.getId());
        return data;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SERVER_API}, allEntries = true),
        }
    )
    @Override
    public boolean saveOrEditBatch(List<ServerApiDTO> entities) {
        if (CollUtil.isEmpty(entities)) {
            return Boolean.FALSE;
        }
        final Integer nextSeq = this.serverApiRepository.getNextSeq();
        AtomicInteger seq = new AtomicInteger(nextSeq);
        List<ServerApiDTO> saveList = new ArrayList<>(10);
        List<ServerApiDTO> editList = new ArrayList<>(10);
        entities.forEach(entity -> {
            final ServerApiDO data = this.serverApiRepository.getOne(Wrappers.lambdaQuery(ServerApiDO.class)
                .eq(ServerApiDO::getAppName, entity.getAppName())
                .eq(ServerApiDO::getControllerName, entity.getControllerName())
                .eq(ServerApiDO::getApiName, entity.getApiName())
                .eq(ServerApiDO::getApiUrl, entity.getApiUrl())
                .select(ServerApiDO::getId, ServerApiDO::getVersion));
            if (Objects.isNull(data)) {
                entity.setSeq(seq.getAndIncrement());
                entity.setRate(2000L);
                saveList.add(entity);
            } else {
                entity.setId(data.getId());
                entity.setVersion(data.getVersion());
                editList.add(entity);
            }
        });
        if (CollUtil.isNotEmpty(saveList)) {
            List<ServerApiDO> doList = BeanUtil.copyToList(saveList, ServerApiDO.class);
            this.serverApiRepository.saveBatch(doList, DEFAULT_BATCH_SIZE);
        }
        if (CollUtil.isNotEmpty(editList)) {
            List<ServerApiDO> doList = BeanUtil.copyToList(editList, ServerApiDO.class);
            this.serverApiRepository.updateBatchById(doList, DEFAULT_BATCH_SIZE);
        }
        return Boolean.TRUE;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SERVER_API}, allEntries = true),
        }
    )
    @Override
    public boolean removeByIds(List<Long> ids) {
        List<ServerApiDO> list = this.serverApiRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }

        return this.serverApiRepository.removeByIds(ids);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SERVER_API}, allEntries = true),
        }
    )
    @Override
    public boolean edit(ServerApiDTO entity) {
        ServerApiDO data = this.serverApiRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (CharSequenceUtil.isNotBlank(entity.getControllerName()) && !CharSequenceUtil.equals(data.getControllerName(),
            entity.getControllerName().trim())) {
            this.serverApiRepository.getControllerNameIsExist(entity.getAppName().trim(), entity.getControllerName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getApiName()) && !CharSequenceUtil.equals(data.getApiName(), entity.getApiName().trim())) {
            this.serverApiRepository.getApiNameIsExist(entity.getAppName().trim(), entity.getApiName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getApiUrl()) && !CharSequenceUtil.equals(data.getApiUrl(), entity.getApiUrl().trim())) {
            this.serverApiRepository.getApiUrlIsExist(entity.getAppName().trim(), entity.getApiUrl().trim());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        ServerApiDO model = BeanUtil.copyProperties(entity, ServerApiDO.class);
        boolean result = this.serverApiRepository.updateById(model);
        return result;
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SERVER_API}, allEntries = true),
        }
    )
    @Override
    public boolean editHideFlag(Long id, Boolean hideFlag) {
        ServerApiDO data = this.serverApiRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (hideFlag.equals(data.getHideFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        ServerApiDO param = new ServerApiDO();
        param.setId(id);
        param.setHideFlag(hideFlag);
        param.setVersion(data.getVersion());
        this.serverApiRepository.updateById(param);
        return Boolean.TRUE;
    }

    @Override
    public IPage<ServerApiPageVO> page(ServerApiSearchDTO entity) {
        PageDTO<ServerApiDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<ServerApiDO> queryWrapper = Wrappers.lambdaQuery(ServerApiDO.class)
            .orderByDesc(ServerApiDO::getModifiedTime);
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
    @Override
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
    @Override
    public List<ServerApiBaseVO> listApi() {
        final List<ServerApiDO> list = this.serverApiRepository.list();
        return this.buildServerApiBaseVO(list);
    }

    @Cacheable(key = "#root.methodName + '_' + #p0", condition = "#p0 != null", unless = "#result == null")
    @Override
    public ServerApiVO getDetail(Long id) {
        ServerApiDO data = this.serverApiRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, ServerApiVO.class);
    }

    private List<ApiVO> listApi(List<ServerApiDO> serverApiList) {
        return serverApiList.stream()
            .map(serverApi -> {
                return ApiVO.builder()
                    .id(serverApi.getId())
                    .requestMethod(serverApi.getRequestMethod())
                    .apiName(serverApi.getApiName())
                    .apiUrl(serverApi.getApiUrl())
                    .build();
            })
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
