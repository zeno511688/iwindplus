/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.flow.domain.dto.FlowInstanceEventDTO;
import com.iwindplus.flow.domain.dto.FlowModelContentDTO;
import com.iwindplus.flow.domain.dto.FlowNodeDTO;
import com.iwindplus.flow.domain.dto.FlowStartInstanceDTO;
import com.iwindplus.flow.domain.enums.FlowCodeEnum;
import com.iwindplus.flow.domain.enums.FlowInstanceEventTypeEnum;
import com.iwindplus.flow.domain.enums.FlowInstanceStatusEnum;
import com.iwindplus.flow.domain.enums.FlowTaskStatusEnum;
import com.iwindplus.flow.domain.vo.FlowModelExtVO;
import com.iwindplus.flow.domain.vo.FlowStartInstanceVO;
import com.iwindplus.flow.server.core.FlowEventService;
import com.iwindplus.flow.server.core.FlowInstanceActionService;
import com.iwindplus.flow.server.core.FlowRuntimeService;
import com.iwindplus.flow.server.core.FlowTaskActionService;
import com.iwindplus.flow.server.dal.model.FlowInstanceDO;
import com.iwindplus.flow.server.dal.model.FlowInstanceExtendDO;
import com.iwindplus.flow.server.dal.repository.FlowInstanceExtendRepository;
import com.iwindplus.flow.server.dal.repository.FlowInstanceRepository;
import com.iwindplus.flow.server.dal.repository.FlowModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流程实例动作业务层接口实现类.
 * <p>
 * 负责流程实例的启动、撤回、终止等操作。 所有方法均在事务保护下执行，确保数据一致性。
 *
 * @author zengdegui
 * @since 2026/05/22 22:51
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowInstanceActionServiceImpl implements FlowInstanceActionService {

    private final RedissonService redissonService;
    private final FlowModelRepository flowModelRepository;
    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowInstanceExtendRepository flowInstanceExtendRepository;
    private final FlowTaskActionService flowTaskActionService;
    private final FlowEventService flowEventService;
    private final FlowRuntimeService flowRuntimeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FlowStartInstanceVO startInstance(FlowStartInstanceDTO entity) {
        FlowModelExtVO model = flowModelRepository.getNewestPublishedByCode(entity.getModelCode());

        FlowModelContentDTO content = model.getModelContent();

        if (content == null || content.getNode() == null) {
            throw new BizException(FlowCodeEnum.FLOW_NODE_NOT_FOUND);
        }

        FlowNodeDTO firstNode = content.getNode().getChildNode();
        if (firstNode == null) {
            throw new BizException(FlowCodeEnum.FLOW_NODE_NOT_FOUND);
        }

        String code = redissonService.serialNum().getSerialNumDate("FI");
        if (CharSequenceUtil.isNotBlank(entity.getBizNumber())) {
            flowInstanceRepository.getBizNumberIsExist(entity.getBizNumber().trim());
        } else {
            entity.setBizNumber(code);
        }

        FlowInstanceDO instance = FlowInstanceDO.builder()
            .code(code)
            .name(model.getName())
            .bizNumber(entity.getBizNumber())
            .modelId(model.getId())
            .currentNodeCode(firstNode.getCode())
            .currentNodeName(firstNode.getName())
            .build();

        flowInstanceRepository.save(instance);

        FlowInstanceExtendDO extend = FlowInstanceExtendDO.builder()
            .instanceId(instance.getId())
            .variable(entity.getVariables() == null ? null
                : JacksonUtil.toJsonStr(entity.getVariables()))
            .build();

        flowInstanceExtendRepository.save(extend);

        flowTaskActionService.createTasksForNode(
            firstNode,
            instance,
            model.getId()
        );

        flowEventService.publishInstanceEvent(
            FlowInstanceEventDTO.builder()
                .eventType(FlowInstanceEventTypeEnum.INSTANCE_STARTED)
                .instanceId(instance.getId())
                .instanceName(instance.getName())
                .instanceCode(instance.getCode())
                .bizNumber(instance.getBizNumber())
                .callbackUrl(entity.getCallbackUrl())
                .operatorId(entity.getCurrentUser().getUserId())
                .operatorName(entity.getCurrentUser().getRealName())
                .build()
        );

        return FlowStartInstanceVO.builder()
            .id(instance.getId())
            .bizNumber(instance.getBizNumber())
            .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revokeInstance(Long instanceId, UserBaseVO currentUser) {
        // 获取并验证实例
        FlowInstanceDO instance = flowRuntimeService.getInstance(instanceId);
        if (instance == null) {
            throw new BizException(FlowCodeEnum.FLOW_INSTANCE_NOT_EXIST);
        }

        // 验证是否为发起人
        Long userId = currentUser.getUserId();
        if (!userId.equals(instance.getCreatedId())) {
            throw new BizException(FlowCodeEnum.FLOW_ONLY_INITIATOR_REVOKE);
        }

        // 归档所有活动任务
        flowRuntimeService.archiveActiveTasksForInstance(instanceId, FlowTaskStatusEnum.REVOKED);

        // 归档实例
        flowRuntimeService.archiveInstance(instance, FlowInstanceStatusEnum.REVOKED);

        // 发布撤回事件
        flowEventService.publishInstanceEvent(
            FlowInstanceEventDTO.builder()
                .eventType(FlowInstanceEventTypeEnum.INSTANCE_REVOKED)
                .instanceId(instance.getId())
                .instanceName(instance.getName())
                .instanceCode(instance.getCode())
                .bizNumber(instance.getBizNumber())
                .operatorId(userId)
                .build()
        );

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean terminateInstance(Long instanceId, UserBaseVO currentUser) {
        // 获取并验证实例
        FlowInstanceDO instance = flowRuntimeService.getInstance(instanceId);
        if (instance == null) {
            throw new BizException(FlowCodeEnum.FLOW_INSTANCE_NOT_EXIST);
        }

        // 归档所有活动任务
        flowRuntimeService.archiveActiveTasksForInstance(instanceId, FlowTaskStatusEnum.TERMINATED);

        // 归档实例
        flowRuntimeService.archiveInstance(instance, FlowInstanceStatusEnum.TERMINATED);

        // 发布终止事件
        flowEventService.publishInstanceEvent(
            FlowInstanceEventDTO.builder()
                .eventType(FlowInstanceEventTypeEnum.INSTANCE_TERMINATED)
                .instanceId(instance.getId())
                .instanceName(instance.getName())
                .instanceCode(instance.getCode())
                .bizNumber(instance.getBizNumber())
                .build()
        );

        return true;
    }
}
