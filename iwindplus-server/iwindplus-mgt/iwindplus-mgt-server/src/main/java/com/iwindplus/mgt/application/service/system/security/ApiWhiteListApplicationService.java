/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.system.security;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.google.common.collect.ImmutableMap;
import com.iwindplus.base.async.task.domain.dto.AsyncTaskSubmitDTO;
import com.iwindplus.base.async.task.executor.AsyncTaskExecutor;
import com.iwindplus.base.domain.dto.MessageBaseDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.OperateTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.executor.RedissonExecutor;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.mgt.application.service.system.security.dto.ApiWhiteListChangeDTO;
import com.iwindplus.mgt.application.service.system.security.dto.ApiWhiteListDTO;
import com.iwindplus.mgt.common.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.common.enums.MgtCodePrefixEnum;
import com.iwindplus.mgt.infrastructure.asynctask.security.ApiWhiteListTaskHandler;
import com.iwindplus.mgt.infrastructure.persistence.system.security.ApiWhiteListDO;
import com.iwindplus.mgt.infrastructure.persistence.system.security.ApiWhiteListRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * API白名单业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/3/25
 */

@Service
@CacheConfig(cacheNames = {RedisCacheConstant.CACHE_API_WHITE_LIST})
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class ApiWhiteListApplicationService {

    private final RedissonExecutor redissonExecutor;
    private final ApiWhiteListRepository apiWhiteListRepository;
    private final AsyncTaskExecutor asyncTaskExecutor;

    @CacheEvict(allEntries = true)
    public boolean save(ApiWhiteListDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        // 校验路径是否存在
        this.apiWhiteListRepository.getNameIsExist(entity.getName());
        this.apiWhiteListRepository.getApiUrlIsExist(entity.getApiUrl());
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            entity.setCode(this.redissonExecutor.serialNum().getSerialNumDate(MgtCodePrefixEnum.API_WHITE_LIST_PREFIX.getValue()));
        }
        this.apiWhiteListRepository.getCodeIsExist(entity.getCode().trim());
        entity.setSeq(this.apiWhiteListRepository.getNextSeq());
        final ApiWhiteListDO model = BeanUtil.copyProperties(entity, ApiWhiteListDO.class);
        this.apiWhiteListRepository.save(model);
        entity.setId(model.getId());
        // 发送消息
        this.sendMsg(OperateTypeEnum.ADD, List.of(entity.getApiUrl()), null);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean removeByIds(List<Long> ids) {
        List<ApiWhiteListDO> list = this.apiWhiteListRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        boolean match = list.stream().filter(Objects::nonNull).anyMatch(ApiWhiteListDO::getBuildInFlag);
        if (Boolean.TRUE.equals(match)) {
            throw new BizException(BizCodeEnum.HAS_BUILD_IN_DATA);
        }
        this.apiWhiteListRepository.removeByIds(ids);

        // 发送消息
        final List<String> oldApiUrl = list.stream().map(ApiWhiteListDO::getApiUrl).collect(Collectors.toList());
        this.sendMsg(OperateTypeEnum.DELETE, null, oldApiUrl);
        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean edit(ApiWhiteListDTO entity) {
        ApiWhiteListDO data = this.apiWhiteListRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (CharSequenceUtil.isNotBlank(entity.getName()) && !CharSequenceUtil.equals(data.getName(), entity.getName().trim())) {
            this.apiWhiteListRepository.getNameIsExist(entity.getName().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getApiUrl()) && !CharSequenceUtil.equals(data.getApiUrl(), entity.getApiUrl().trim())) {
            this.apiWhiteListRepository.getApiUrlIsExist(entity.getApiUrl().trim());
        }
        if (CharSequenceUtil.isNotBlank(entity.getCode()) && !CharSequenceUtil.equals(data.getCode(), entity.getCode().trim())) {
            this.apiWhiteListRepository.getCodeIsExist(entity.getCode().trim());
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final ApiWhiteListDO model = BeanUtil.copyProperties(entity, ApiWhiteListDO.class);
        this.apiWhiteListRepository.updateById(model);

        // 发送消息
        if (EnableStatusEnum.ENABLE.equals(entity.getStatus())) {
            this.sendMsg(OperateTypeEnum.MODIFY, List.of(entity.getApiUrl()), List.of(data.getApiUrl()));
        } else if (EnableStatusEnum.DISABLE.equals(entity.getStatus())
            || EnableStatusEnum.LOCKED.equals(entity.getStatus())) {
            this.sendMsg(OperateTypeEnum.DELETE, null, List.of(data.getApiUrl()));
        }

        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean editStatus(Long id, EnableStatusEnum status) {
        ApiWhiteListDO data = this.apiWhiteListRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.BUILD_IN_DATA_NOT_OPERATE);
        }
        if (status.equals(data.getStatus())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        ApiWhiteListDO param = new ApiWhiteListDO();
        param.setId(id);
        param.setStatus(status);
        param.setVersion(data.getVersion());
        this.apiWhiteListRepository.updateById(param);

        // 发送消息
        if (EnableStatusEnum.ENABLE.equals(status)) {
            this.sendMsg(OperateTypeEnum.ADD, List.of(data.getApiUrl()), null);
        } else if (EnableStatusEnum.DISABLE.equals(status)
            || EnableStatusEnum.LOCKED.equals(status)) {
            this.sendMsg(OperateTypeEnum.DELETE, null, List.of(data.getApiUrl()));
        }

        return Boolean.TRUE;
    }

    @CacheEvict(allEntries = true)
    public boolean editBuildIn(Long id, Boolean buildInFlag) {
        ApiWhiteListDO data = this.apiWhiteListRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (buildInFlag.equals(data.getBuildInFlag())) {
            throw new BizException(BizCodeEnum.ALREADY_OPERATED);
        }
        ApiWhiteListDO param = new ApiWhiteListDO();
        param.setId(id);
        param.setBuildInFlag(buildInFlag);
        param.setVersion(data.getVersion());
        this.apiWhiteListRepository.updateById(param);
        return Boolean.TRUE;
    }

    private boolean sendMsg(OperateTypeEnum operateType, List<String> newApiUrl, List<String> oldApiUrl) {
        if (CollUtil.isEmpty(newApiUrl)) {
            return false;
        }

        final MessageBaseDTO<ApiWhiteListChangeDTO> messageDTO = new MessageBaseDTO<>();
        messageDTO.setOperateType(operateType.getValue());
        messageDTO.setBizType("apiWhiteList");

        ApiWhiteListChangeDTO apiWhiteListChangeDTO = new ApiWhiteListChangeDTO();
        apiWhiteListChangeDTO.setNewApiUrl(newApiUrl);
        if (CollUtil.isNotEmpty(oldApiUrl)) {
            apiWhiteListChangeDTO.setOldApiUrl(oldApiUrl);
        }
        messageDTO.setData(apiWhiteListChangeDTO);
        final String content = JacksonUtil.toJsonStr(messageDTO);

        final AsyncTaskSubmitDTO build = AsyncTaskSubmitDTO.builder()
            .bizName("API白名单数据发送kafka")
            .bizKey("API_WHITE_LIST")
            .bizType("API_WHITE_LIST_PUSH")
            .param(ImmutableMap.of("content", content))
            .executorClass(ApiWhiteListTaskHandler.class)
            .remark("API白名单数据发送kafka")
            .build();
        this.asyncTaskExecutor.submit(build);
        return true;
    }
}
