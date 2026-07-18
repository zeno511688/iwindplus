/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.server.service.system.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.ImmutableMap;
import com.iwindplus.base.async.cmd.domain.bo.AsyncCmdExecutorBO;
import com.iwindplus.base.async.cmd.executor.AsyncCmdExecutor;
import com.iwindplus.base.domain.dto.MessageBaseDTO;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.OperateTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.mgt.domain.constant.MgtConstant.RedisCacheConstant;
import com.iwindplus.mgt.domain.dto.system.ApiWhiteListChangeDTO;
import com.iwindplus.mgt.domain.dto.system.ApiWhiteListDTO;
import com.iwindplus.mgt.domain.dto.system.ApiWhiteListSearchDTO;
import com.iwindplus.mgt.domain.enums.MgtCodePrefixEnum;
import com.iwindplus.mgt.domain.vo.system.ApiWhiteListPageVO;
import com.iwindplus.mgt.domain.vo.system.ApiWhiteListVO;
import com.iwindplus.mgt.server.dal.model.system.ApiWhiteListDO;
import com.iwindplus.mgt.server.dal.repository.system.ApiWhiteListRepository;
import com.iwindplus.mgt.server.service.asynccmd.ApiWhiteListTaskHandler;
import com.iwindplus.mgt.server.service.system.ApiWhiteListService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
public class ApiWhiteListServiceImpl implements ApiWhiteListService {

    private final RedissonService redissonService;
    private final ApiWhiteListRepository apiWhiteListRepository;
    private final AsyncCmdExecutor asyncCmdExecutor;

    @CacheEvict(allEntries = true)
    @Override
    public boolean save(ApiWhiteListDTO entity) {
        entity.setStatus(EnableStatusEnum.ENABLE);
        // 校验路径是否存在
        this.apiWhiteListRepository.getNameIsExist(entity.getName());
        this.apiWhiteListRepository.getApiUrlIsExist(entity.getApiUrl());
        if (CharSequenceUtil.isBlank(entity.getCode())) {
            entity.setCode(this.redissonService.serialNum().getSerialNumDate(MgtCodePrefixEnum.API_WHITE_LIST_PREFIX.getValue()));
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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

    @Override
    public IPage<ApiWhiteListPageVO> page(ApiWhiteListSearchDTO entity) {
        PageDTO<ApiWhiteListDO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        LambdaQueryWrapper<ApiWhiteListDO> queryWrapper = Wrappers.lambdaQuery(ApiWhiteListDO.class)
            .orderByDesc(ApiWhiteListDO::getModifiedTime);
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
    @Override
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
    @Override
    public ApiWhiteListVO getDetail(Long id) {
        ApiWhiteListDO data = this.apiWhiteListRepository.getById(id);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        return BeanUtil.copyProperties(data, ApiWhiteListVO.class);
    }

    private boolean sendMsg(OperateTypeEnum operateType, List<String> newApiUrl, List<String> oldApiUrl) {
        if (CollUtil.isEmpty(newApiUrl)) {
            return false;
        }

        final MessageBaseDTO<ApiWhiteListChangeDTO> messageDTO = new MessageBaseDTO();
        messageDTO.setOperateType(operateType.getValue());
        messageDTO.setBizType("apiWhiteList");

        ApiWhiteListChangeDTO apiWhiteListChangeDTO = new ApiWhiteListChangeDTO();
        apiWhiteListChangeDTO.setNewApiUrl(newApiUrl);
        if (CollUtil.isNotEmpty(oldApiUrl)) {
            apiWhiteListChangeDTO.setOldApiUrl(oldApiUrl);
        }
        messageDTO.setData(apiWhiteListChangeDTO);
        final String content = JacksonUtil.toJsonStr(messageDTO);

        final AsyncCmdExecutorBO build = AsyncCmdExecutorBO.builder()
            .bizType("API_WHITE_LIST")
            .eventType("API_WHITE_LIST_PUSH")
            .bizNumber(IdUtil.fastSimpleUUID())
            .content(ImmutableMap.of("content", content))
            .executorClass(ApiWhiteListTaskHandler.class)
            .remark("API白名单数据发送kafka")
            .build();
        this.asyncCmdExecutor.submit(build);
        return true;
    }
}
