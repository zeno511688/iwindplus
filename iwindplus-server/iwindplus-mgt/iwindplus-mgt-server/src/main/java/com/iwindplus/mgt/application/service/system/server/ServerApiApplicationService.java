/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.system.server;

import static com.baomidou.mybatisplus.extension.repository.IRepository.DEFAULT_BATCH_SIZE;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.mgt.application.service.system.server.dto.ServerApiDTO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.infrastructure.persistence.system.server.ServerApiDO;
import com.iwindplus.mgt.infrastructure.persistence.system.server.ServerApiRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
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
public class ServerApiApplicationService {

    private final ServerApiRepository serverApiRepository;

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SERVER_API}, allEntries = true),
        }
    )
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
        return this.serverApiRepository.updateById(model);
    }

    @Caching(
        evict = {
            @CacheEvict(cacheNames = {RedisCacheConstant.CACHE_SERVER_API}, allEntries = true),
        }
    )
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
}
