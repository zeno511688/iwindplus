/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.interfaces.listener;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.flow.application.query.instance.vo.FlowInstanceCallbackVO;
import com.iwindplus.flow.application.service.instance.FlowInstanceCallbackApplicationService;
import com.iwindplus.flow.application.service.instance.dto.FlowInstanceCallbackSaveDTO;
import com.iwindplus.flow.application.service.instance.dto.FlowInstanceEventDTO;
import com.iwindplus.flow.common.enums.FlowInstanceCallbackStatusEnum;
import com.iwindplus.flow.common.enums.FlowInstanceEventTypeEnum;
import com.iwindplus.flow.domain.engine.FlowInstanceActionListener;
import com.iwindplus.flow.infrastructure.persistence.base.FlowCategoryDO;
import com.iwindplus.flow.infrastructure.persistence.base.FlowCategoryRepository;
import com.iwindplus.flow.infrastructure.persistence.instance.FlowInstanceCallbackDO;
import com.iwindplus.flow.infrastructure.persistence.instance.FlowInstanceCallbackRepository;
import com.iwindplus.flow.infrastructure.persistence.instance.FlowInstanceDO;
import com.iwindplus.flow.infrastructure.persistence.instance.FlowInstanceExtendDO;
import com.iwindplus.flow.infrastructure.persistence.instance.FlowInstanceExtendRepository;
import com.iwindplus.flow.infrastructure.persistence.instance.FlowInstanceRepository;
import com.iwindplus.flow.infrastructure.persistence.model.FlowModelDO;
import com.iwindplus.flow.infrastructure.persistence.model.FlowModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 流程实例事件监听.
 *
 * @author zengdegui
 * @since 2026/06/14 21:23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlowInstanceListener implements FlowInstanceActionListener {

    private final FlowInstanceCallbackApplicationService flowInstanceCallbackService;
    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowInstanceExtendRepository flowInstanceExtendRepository;
    private final FlowModelRepository flowModelRepository;
    private final FlowCategoryRepository flowCategoryRepository;
    private final FlowInstanceCallbackRepository flowInstanceCallbackRepository;

    @Override
    public void onEvent(FlowInstanceEventDTO event) {
        if (event == null || event.getInstanceId() == null) {
            return;
        }

        try {
            // 流程启动事件 - 插入回调记录
            if (FlowInstanceEventTypeEnum.INSTANCE_STARTED.equals(event.getEventType())) {
                handleInstanceStarted(event);
            }
            // 流程完成事件 - 更新回调状态
            else if (FlowInstanceEventTypeEnum.INSTANCE_FINISHED.equals(event.getEventType())) {
                handleInstanceFinished(event);
            }
        } catch (Exception e) {
            log.error("流程实例事件处理失败 instanceId={}", event.getInstanceId(), e);
        }
    }

    /**
     * 处理流程启动事件.
     */
    private void handleInstanceStarted(FlowInstanceEventDTO event) {
        Long instanceId = event.getInstanceId();
        log.info("流程启动，准备插入回调记录 instanceId={}", instanceId);

        // 获取流程实例
        FlowInstanceDO instance = flowInstanceRepository.getById(instanceId);
        if (instance == null) {
            log.warn("流程实例不存在 instanceId={}", instanceId);
            return;
        }

        // 获取流程扩展信息（包含变量）
        final FlowInstanceExtendDO extend = flowInstanceExtendRepository.getByInstanceId(instanceId);
        if (extend == null) {
            return;
        }

        final FlowModelDO model = flowModelRepository.getById(instance.getModelId());
        if (model == null) {
            return;
        }

        final FlowCategoryDO category = flowCategoryRepository.getById(model.getCategoryId());
        if (category == null) {
            return;
        }

        // 构建回调保存DTO
        FlowInstanceCallbackSaveDTO callbackDTO = FlowInstanceCallbackSaveDTO.builder()
            .status(FlowInstanceCallbackStatusEnum.PENDING)
            .instanceId(instance.getId())
            .instanceName(instance.getName())
            .bizNumber(instance.getBizNumber())
            .modelId(instance.getModelId())
            .modelName(model.getName())
            .categoryId(category.getId())
            .categoryName(category.getName())
            .callbackUrl(event.getCallbackUrl())
            .variable(extend.getVariable())
            .retryCount(0)
            .build();

        // 保存回调记录
        flowInstanceCallbackService.save(callbackDTO);
        log.info("流程启动回调记录保存成功 instanceId={}", instanceId);
    }

    /**
     * 处理流程完成事件.
     */
    private void handleInstanceFinished(FlowInstanceEventDTO event) {
        Long instanceId = event.getInstanceId();
        log.info("流程完成，准备更新回调状态并执行外部回调 instanceId={}", instanceId);

        // 查询回调记录
        FlowInstanceCallbackDO callback = flowInstanceCallbackRepository.getByInstanceId(instanceId);
        if (callback == null) {
            log.warn("流程完成回调状态更新失败，未找到回调记录 instanceId={}", instanceId);
            return;
        }

        // 检查是否有回调地址
        if (CharSequenceUtil.isBlank(callback.getCallbackUrl())) {
            log.info("流程完成，但没有配置回调地址，仅更新状态 instanceId={}", instanceId);
            return;
        }

        // 执行外部回调（包含HTTP请求和状态更新）
        final FlowInstanceCallbackVO flowInstanceCallbackVO = BeanUtil.copyProperties(callback, FlowInstanceCallbackVO.class);
        boolean success = flowInstanceCallbackService.executeCallback(flowInstanceCallbackVO);
        if (success) {
            log.info("流程完成回调执行成功 instanceId={}, callbackId={}", instanceId, callback.getId());
        } else {
            log.error("流程完成回调执行失败 instanceId={}, callbackId={}", instanceId, callback.getId());
        }
    }
}
