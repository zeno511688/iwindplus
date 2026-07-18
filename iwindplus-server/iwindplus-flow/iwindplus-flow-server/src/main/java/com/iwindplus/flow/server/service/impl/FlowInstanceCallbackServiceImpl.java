/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.flow.domain.dto.FlowInstanceCallbackEditDTO;
import com.iwindplus.flow.domain.dto.FlowInstanceCallbackExtDTO;
import com.iwindplus.flow.domain.dto.FlowInstanceCallbackSaveDTO;
import com.iwindplus.flow.domain.enums.FlowCodeEnum;
import com.iwindplus.flow.domain.enums.FlowInstanceCallbackStatusEnum;
import com.iwindplus.flow.server.config.property.FlowProperty;
import com.iwindplus.flow.server.dal.model.FlowCategoryDO;
import com.iwindplus.flow.server.dal.model.FlowHisInstanceDO;
import com.iwindplus.flow.server.dal.model.FlowInstanceCallbackDO;
import com.iwindplus.flow.server.dal.model.FlowInstanceDO;
import com.iwindplus.flow.server.dal.model.FlowModelDO;
import com.iwindplus.flow.server.dal.repository.FlowCategoryRepository;
import com.iwindplus.flow.server.dal.repository.FlowHisInstanceRepository;
import com.iwindplus.flow.server.dal.repository.FlowInstanceCallbackRepository;
import com.iwindplus.flow.server.dal.repository.FlowInstanceRepository;
import com.iwindplus.flow.server.dal.repository.FlowModelRepository;
import com.iwindplus.flow.server.service.FlowInstanceCallbackService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流程实例回调业务层接口实现类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FlowInstanceCallbackServiceImpl implements FlowInstanceCallbackService {

    private final FlowInstanceCallbackRepository flowInstanceCallbackRepository;
    private final FlowCategoryRepository flowCategoryRepository;
    private final FlowModelRepository flowModelRepository;
    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowHisInstanceRepository flowHisInstanceRepository;
    private final HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory;
    private final FlowProperty flowProperty;

    @Override
    public boolean save(FlowInstanceCallbackSaveDTO entity) {
        final FlowCategoryDO flowCategory = this.flowCategoryRepository.getById(entity.getCategoryId());
        if (flowCategory == null) {
            throw new BizException(FlowCodeEnum.FLOW_CATEGORY_NOT_EXIST);
        }
        final FlowInstanceDO flowInstance = this.flowInstanceRepository.getById(entity.getInstanceId());
        if (flowInstance == null) {
            throw new BizException(FlowCodeEnum.FLOW_INSTANCE_NOT_EXIST);
        }
        final FlowModelDO flowModel = this.flowModelRepository.getById(entity.getModelId());
        if (flowModel == null) {
            throw new BizException(FlowCodeEnum.FLOW_MODEL_NOT_EXIST);
        }

        final FlowInstanceCallbackDO model = BeanUtil.copyProperties(entity, FlowInstanceCallbackDO.class);
        this.flowInstanceCallbackRepository.save(model);
        return Boolean.TRUE;
    }

    @Override
    public boolean removeByIds(List<Long> ids) {
        List<FlowInstanceCallbackDO> list = this.flowInstanceCallbackRepository.listByIds(ids);
        if (CollUtil.isEmpty(list)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        this.flowInstanceCallbackRepository.removeByIds(ids);
        return Boolean.TRUE;
    }

    @Override
    public boolean edit(FlowInstanceCallbackEditDTO entity) {
        FlowInstanceCallbackDO data = this.flowInstanceCallbackRepository.getById(entity.getId());
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (entity.getCategoryId() != null && !data.getCategoryId().equals(entity.getCategoryId())) {
            final FlowCategoryDO flowCategory = this.flowCategoryRepository.getById(entity.getCategoryId());
            if (flowCategory == null) {
                throw new BizException(FlowCodeEnum.FLOW_CATEGORY_NOT_EXIST);
            }
        }
        if (entity.getInstanceId() != null && !data.getInstanceId().equals(entity.getInstanceId())) {
            final FlowInstanceDO flowInstance = this.flowInstanceRepository.getById(entity.getInstanceId());
            if (flowInstance == null) {
                throw new BizException(FlowCodeEnum.FLOW_INSTANCE_NOT_EXIST);
            }
        }
        if (entity.getModelId() != null && !data.getModelId().equals(entity.getModelId())) {
            final FlowModelDO flowModel = this.flowModelRepository.getById(entity.getModelId());
            if (flowModel == null) {
                throw new BizException(FlowCodeEnum.FLOW_MODEL_NOT_EXIST);
            }
        }
        if (Objects.isNull(entity.getVersion())) {
            entity.setVersion(data.getVersion());
        }
        final FlowInstanceCallbackDO model = BeanUtil.copyProperties(entity, FlowInstanceCallbackDO.class);
        this.flowInstanceCallbackRepository.updateById(model);
        return Boolean.TRUE;
    }

    @Override
    public boolean executeCallback(FlowInstanceCallbackDO callback) {
        Long callbackId = callback.getId();
        String callbackUrl = callback.getCallbackUrl();

        // 检查回调地址是否为空
        if (StrUtil.isBlank(callbackUrl)) {
            log.warn("回调地址为空，跳过处理 callbackId={}", callbackId);
            return false;
        }

        try {
            // 构建外部应用回调DTO
            FlowInstanceCallbackExtDTO extDTO = buildCallbackExtDTO(callback);
            if (extDTO == null) {
                return false;
            }

            // 发起HTTP POST请求
            log.info("发起回调请求 callbackId={}, url={}", callbackId, callbackUrl);
            final ResultVO<Boolean> result = httpClientExecutorStrategyFactory
                .getHttpClientExecutor(HttpClientTypeEnum.REST_CLIENT)
                .post(
                    callbackUrl,
                    extDTO,
                    null,
                    new TypeReference<>() {
                    }
                );
            result.errorThrow();
            Boolean data = result.getBizData();

            // 请求成功，更新状态为完成
            log.info("回调请求成功 callbackId={}, response={}", callbackId, data);
            updateCallbackStatus(callback, FlowInstanceCallbackStatusEnum.COMPLETE);
            return true;
        } catch (Exception e) {
            log.error("回调请求失败 callbackId={}, retryCount={}", callbackId, callback.getRetryCount(), e);

            // 增加重试次数
            int newRetryCount = callback.getRetryCount() == null ? 1 : callback.getRetryCount() + 1;

            if (newRetryCount >= flowProperty.getMaxRetry()) {
                // 超过最大重试次数，更新状态为丢弃
                log.warn("回调重试次数已达上限，状态更新为丢弃 callbackId={}, retryCount={}",
                    callbackId, newRetryCount);
                updateCallbackToFailed(callback, newRetryCount, FlowInstanceCallbackStatusEnum.DISCARD);
            } else {
                // 更新状态为失败，等待下次重试
                log.info("回调失败，等待下次重试 callbackId={}, newRetryCount={}",
                    callbackId, newRetryCount);
                updateCallbackToFailed(callback, newRetryCount, FlowInstanceCallbackStatusEnum.FAILED);
            }

            return false;
        }
    }

    /**
     * 构建外部应用回调DTO.
     *
     * @param callback 回调记录
     * @return FlowInstanceCallbackExtDTO
     */
    private FlowInstanceCallbackExtDTO buildCallbackExtDTO(FlowInstanceCallbackDO callback) {
        // 根据当前状态推断实例状态
        final FlowHisInstanceDO hisInstance = this.flowHisInstanceRepository.getById(callback.getInstanceId());
        if (hisInstance == null) {
            return null;
        }

        return FlowInstanceCallbackExtDTO.builder()
            .instanceStatus(hisInstance.getStatus())
            .instanceId(callback.getInstanceId())
            .bizNumber(callback.getBizNumber())
            .modelCode(getModelCode(callback.getModelId()))
            .variable(callback.getVariable())
            .build();
    }

    /**
     * 获取模型编码.
     *
     * @param modelId 模型ID
     * @return 模型编码
     */
    private String getModelCode(Long modelId) {
        if (modelId == null) {
            return null;
        }
        FlowModelDO model = flowModelRepository.getById(modelId);
        return model != null ? model.getCode() : null;
    }

    /**
     * 更新回调状态.
     *
     * @param callback 回调记录
     * @param status   新状态
     */
    private void updateCallbackStatus(FlowInstanceCallbackDO callback, FlowInstanceCallbackStatusEnum status) {
        FlowInstanceCallbackDO model = FlowInstanceCallbackDO
            .builder()
            .id(callback.getId())
            .status(status)
            .version(callback.getVersion())
            .build();
        this.flowInstanceCallbackRepository.updateById(model);
    }

    /**
     * 更新回调为失败状态（包含重试次数）.
     *
     * @param callback      回调记录
     * @param newRetryCount 新的重试次数
     * @param status        状态（FAILED或DISCARD）
     */
    private void updateCallbackToFailed(FlowInstanceCallbackDO callback, int newRetryCount,
        FlowInstanceCallbackStatusEnum status) {
        FlowInstanceCallbackDO model = FlowInstanceCallbackDO
            .builder()
            .id(callback.getId())
            .status(status)
            .retryCount(newRetryCount)
            .version(callback.getVersion())
            .build();
        this.flowInstanceCallbackRepository.updateById(model);
    }
}
