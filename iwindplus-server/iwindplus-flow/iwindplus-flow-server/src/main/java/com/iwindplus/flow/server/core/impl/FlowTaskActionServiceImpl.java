/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.flow.domain.dto.FlowAddTaskPlayerDTO;
import com.iwindplus.flow.domain.dto.FlowApproveTaskDTO;
import com.iwindplus.flow.domain.dto.FlowConditionNodeDTO;
import com.iwindplus.flow.domain.dto.FlowDelegateTaskDTO;
import com.iwindplus.flow.domain.dto.FlowInstanceEventDTO;
import com.iwindplus.flow.domain.dto.FlowJumpTaskDTO;
import com.iwindplus.flow.domain.dto.FlowNodeDTO;
import com.iwindplus.flow.domain.dto.FlowNodePlayerDTO;
import com.iwindplus.flow.domain.dto.FlowRejectTaskDTO;
import com.iwindplus.flow.domain.dto.FlowRemoveTaskPlayerDTO;
import com.iwindplus.flow.domain.dto.FlowTaskEventDTO;
import com.iwindplus.flow.domain.dto.FlowTransferTaskDTO;
import com.iwindplus.flow.domain.enums.ApprovalMethodEnum;
import com.iwindplus.flow.domain.enums.ApprovalTypeEnum;
import com.iwindplus.flow.domain.enums.FlowCodeEnum;
import com.iwindplus.flow.domain.enums.FlowInstanceEventTypeEnum;
import com.iwindplus.flow.domain.enums.FlowInstanceStatusEnum;
import com.iwindplus.flow.domain.enums.FlowNodeTypeEnum;
import com.iwindplus.flow.domain.enums.FlowTaskEventTypeEnum;
import com.iwindplus.flow.domain.enums.FlowTaskPlayerTypeEnum;
import com.iwindplus.flow.domain.enums.FlowTaskStatusEnum;
import com.iwindplus.flow.domain.enums.FlowTaskTypeEnum;
import com.iwindplus.flow.domain.vo.FlowApprovalResultVO;
import com.iwindplus.flow.server.core.FlowEventService;
import com.iwindplus.flow.server.core.FlowRuntimeService;
import com.iwindplus.flow.server.core.FlowTaskActionService;
import com.iwindplus.flow.server.core.FlowVariableService;
import com.iwindplus.flow.server.core.factory.FlowApprovalStrategyFactory;
import com.iwindplus.flow.server.core.support.FlowAssigneeResolver;
import com.iwindplus.flow.server.core.support.FlowNodeRouter;
import com.iwindplus.flow.server.core.support.FlowPlayerChecker;
import com.iwindplus.flow.server.dal.model.FlowInstanceDO;
import com.iwindplus.flow.server.dal.model.FlowModelExtendDO;
import com.iwindplus.flow.server.dal.model.FlowTaskDO;
import com.iwindplus.flow.server.dal.model.FlowTaskPlayerDO;
import com.iwindplus.flow.server.dal.repository.FlowInstanceRepository;
import com.iwindplus.flow.server.dal.repository.FlowModelExtendRepository;
import com.iwindplus.flow.server.dal.repository.FlowTaskPlayerRepository;
import com.iwindplus.flow.server.dal.repository.FlowTaskRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流程任务动作业务层接口实现类.
 * <p>
 * 提供流程任务的核心操作功能，包括审批、驳回、跳转、转交、委托、加签、减签等。 所有操作均在事务保护下执行，确保数据一致性。
 *
 * @author zengdegui
 * @since 2026/05/22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowTaskActionServiceImpl implements FlowTaskActionService {

    /**
     * 委托变量前缀，用于存储委托任务的原始处理人ID.
     */
    public static final String DELEGATED_TASK_PREFIX = "_delegated_";

    private final RedissonService redissonService;

    private final FlowTaskRepository flowTaskRepository;
    private final FlowTaskPlayerRepository flowTaskPlayerRepository;
    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowModelExtendRepository flowModelExtendRepository;

    private final FlowRuntimeService flowRuntimeService;
    private final FlowVariableService flowVariableService;
    private final FlowEventService flowEventService;

    private final FlowNodeRouter flowNodeRouter;
    private final FlowApprovalStrategyFactory approvalStrategyFactory;

    private final ObjectProvider<FlowAssigneeResolver> assigneeResolverProvider;
    private final ObjectProvider<FlowPlayerChecker> playerCheckerProvider;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveTask(FlowApproveTaskDTO entity) {
        // 锁定任务并验证权限
        FlowTaskDO task = flowTaskRepository.getTaskForLock(entity.getTaskId());
        List<FlowTaskPlayerDO> players = flowTaskPlayerRepository.listTaskPlayers(task.getId());
        FlowTaskPlayerDO current = getCurrentPlayer(players, entity.getCurrentUser().getOrgId()
            , entity.getCurrentUser().getUserId());

        // 合并流程变量
        if (CollUtil.isNotEmpty(entity.getVariables())) {
            flowVariableService.mergeVariables(task.getInstanceId(), entity.getVariables());
        }

        // 获取节点配置和审批策略
        FlowNodeDTO node = findNodeByModelId(task.getModelId(), task.getNodeCode());
        ApprovalMethodEnum method = node.getApprovalMethod() == null
            ? ApprovalMethodEnum.OR_SIGN
            : node.getApprovalMethod();

        // 执行审批策略
        FlowApprovalResultVO result = approvalStrategyFactory
            .getApprovalHandler(method)
            .approve(task, players, current, node, entity);

        // 保存中间审批记录
        if (Boolean.TRUE.equals(result.getRecordIntermediate())) {
            flowRuntimeService.saveIntermediateApprovalRecord(task, current, entity.getComment());
        }

        // 如果审批未通过，直接返回
        if (Boolean.FALSE.equals(result.getApproved())) {
            return false;
        }

        // 归档已完成的任务
        flowRuntimeService.archiveTasks(List.of(task), FlowTaskStatusEnum.AUDITED, entity.getComment(), result.getRemovePlayerIds());

        // 发布任务审批事件
        publishTaskApprovedEvent(task, entity);

        // 流转到下一节点或结束流程
        FlowInstanceDO instance = flowInstanceRepository.getById(task.getInstanceId());
        Map<String, Object> vars = flowVariableService.loadVariables(instance.getId());
        FlowNodeDTO next = flowNodeRouter.resolveNextNode(node.getChildNode(), vars);
        moveToNextNodeOrFinish(next, instance, task.getModelId());

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectTask(FlowRejectTaskDTO entity) {
        // 锁定任务并验证权限
        FlowTaskDO task = flowTaskRepository.getTaskForLock(entity.getTaskId());
        List<FlowTaskPlayerDO> players = flowTaskPlayerRepository.listTaskPlayers(task.getId());

        // 验证当前用户是否有权限操作此任务
        getCurrentPlayer(players, entity.getCurrentUser().getOrgId(), entity.getCurrentUser().getUserId());

        // 归档当前实例的所有活动任务
        flowRuntimeService.archiveActiveTasksForInstance(task.getInstanceId(), FlowTaskStatusEnum.REJECTED);

        FlowInstanceDO instance = flowInstanceRepository.getById(task.getInstanceId());

        // 如果没有指定目标节点，则终止流程
        if (CharSequenceUtil.isBlank(entity.getTargetNodeCode())) {
            flowRuntimeService.archiveInstance(instance, FlowInstanceStatusEnum.REJECTED);
            publishTaskRejectedEvent(task, entity);
            return true;
        }

        // 查找目标节点并验证
        FlowNodeDTO target = findNodeByModelId(task.getModelId(), entity.getTargetNodeCode());
        if (!FlowNodeTypeEnum.APPROVAL_NODE.equals(target.getNodeType())) {
            throw new BizException(FlowCodeEnum.FLOW_REJECT_NODE_INVALID);
        }

        // 跳转到目标节点
        moveInstanceToNode(instance, target);
        createTasksForNode(target, instance, task.getModelId());

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean jumpTask(FlowJumpTaskDTO entity) {
        // 锁定任务并验证权限
        FlowTaskDO task = flowTaskRepository.getTaskForLock(entity.getTaskId());
        List<FlowTaskPlayerDO> players = flowTaskPlayerRepository.listTaskPlayers(task.getId());

        // 验证当前用户是否有权限操作此任务
        getCurrentPlayer(players, entity.getCurrentUser().getOrgId(), entity.getCurrentUser().getUserId());

        // 查找目标节点
        FlowNodeDTO target = findNodeByModelId(task.getModelId(), entity.getTargetNodeCode());

        // 验证目标节点类型（只能是审批节点或结束节点）
        if (!FlowNodeTypeEnum.APPROVAL_NODE.equals(target.getNodeType())
            && !FlowNodeTypeEnum.END.equals(target.getNodeType())) {
            throw new BizException(FlowCodeEnum.FLOW_JUMP_NODE_INVALID);
        }

        // 归档当前活动任务
        flowRuntimeService.archiveActiveTasksForInstance(task.getInstanceId(), FlowTaskStatusEnum.AUDITED);

        FlowInstanceDO instance = flowInstanceRepository.getById(task.getInstanceId());

        // 如果目标是结束节点，则完成流程
        if (FlowNodeTypeEnum.END.equals(target.getNodeType())) {
            flowRuntimeService.archiveInstance(instance, FlowInstanceStatusEnum.AUDITED);
            publishInstanceFinishedEvent(instance);
            return true;
        }

        // 跳转到目标节点
        moveInstanceToNode(instance, target);
        createTasksForNode(target, instance, task.getModelId());

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean transferTask(FlowTransferTaskDTO entity) {
        // 锁定任务并验证权限
        FlowTaskDO task = flowTaskRepository.getTaskForLock(entity.getTaskId());
        List<FlowTaskPlayerDO> players = flowTaskPlayerRepository.listTaskPlayers(task.getId());
        FlowTaskPlayerDO from = getCurrentPlayer(players, entity.getCurrentUser().getOrgId()
            , entity.getCurrentUser().getUserId());

        // 移除原处理人
        flowTaskPlayerRepository.removeById(from.getId());

        // 添加新处理人（保持原序号）
        flowTaskPlayerRepository.save(
            FlowTaskPlayerDO.builder()
                .taskId(task.getId())
                .instanceId(task.getInstanceId())
                .modelId(task.getModelId())
                .playerId(entity.getToUserId())
                .playerName(entity.getToUserName())
                .type(FlowTaskPlayerTypeEnum.USER)
                .seq(from.getSeq())
                .build()
        );

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delegateTask(FlowDelegateTaskDTO entity) {
        // 禁止委托给自己
        if (entity.getCurrentUser().getUserId().equals(entity.getToUserId())) {
            throw new BizException(FlowCodeEnum.FLOW_DELEGATE_TO_SELF);
        }

        // 锁定任务并验证权限
        FlowTaskDO task = flowTaskRepository.getTaskForLock(entity.getTaskId());
        List<FlowTaskPlayerDO> players = flowTaskPlayerRepository.listTaskPlayers(task.getId());
        FlowTaskPlayerDO from = getCurrentPlayer(players, entity.getCurrentUser().getOrgId()
            , entity.getCurrentUser().getUserId());

        // 添加被委托人作为新的处理人
        flowTaskPlayerRepository.save(
            FlowTaskPlayerDO.builder()
                .taskId(task.getId())
                .instanceId(task.getInstanceId())
                .modelId(task.getModelId())
                .playerId(entity.getToUserId())
                .playerName(entity.getToUserName())
                .type(FlowTaskPlayerTypeEnum.USER)
                .seq(from.getSeq())
                .build()
        );

        // 移除原处理人
        flowTaskPlayerRepository.removeById(from.getId());

        // 保存委托关系变量
        Map<String, Object> vars = new HashMap<>(4);
        vars.put(DELEGATED_TASK_PREFIX + task.getId(), entity.getCurrentUser().getUserId());
        flowVariableService.mergeVariables(task.getInstanceId(), vars);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addTaskPlayer(FlowAddTaskPlayerDTO entity) {
        FlowTaskDO task = flowTaskRepository.getTaskForLock(entity.getTaskId());

        // 获取当前任务的最大序号
        Integer maxSeq = flowTaskPlayerRepository.getMaxSeqByTaskId(task.getId());

        flowTaskPlayerRepository.save(
            FlowTaskPlayerDO.builder()
                .taskId(task.getId())
                .instanceId(task.getInstanceId())
                .modelId(task.getModelId())
                .playerId(entity.getUserId())
                .playerName(entity.getUserName())
                .type(FlowTaskPlayerTypeEnum.USER)
                .seq(maxSeq + 1)
                .build()
        );

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeTaskPlayer(FlowRemoveTaskPlayerDTO entity) {
        FlowTaskDO task = flowTaskRepository.getTaskForLock(entity.getTaskId());

        List<FlowTaskPlayerDO> players = flowTaskPlayerRepository.listTaskPlayers(task.getId());

        if (players.size() <= 1) {
            throw new BizException(FlowCodeEnum.FLOW_KEEP_AT_LEAST_ONE_PLAYER);
        }

        FlowTaskPlayerDO current = getCurrentPlayer(players, entity.getCurrentUser().getOrgId(), entity.getUserId());

        flowTaskPlayerRepository.removeById(current.getId());

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTasksForNode(
        FlowNodeDTO node,
        FlowInstanceDO instance,
        Long modelId) {

        if (node == null) {
            return;
        }

        if (FlowNodeTypeEnum.CC_NODE.equals(node.getNodeType())) {
            handleCcNode(node, instance, modelId);
            return;
        }

        if (FlowNodeTypeEnum.CONDITION_NODE.equals(node.getNodeType())) {
            handleConditionNode(node, instance, modelId);
            return;
        }

        if (FlowNodeTypeEnum.APPROVAL_NODE.equals(node.getNodeType())) {
            handleApprovalNode(node, instance, modelId);
        }
    }

    public void handleCcNode(
        FlowNodeDTO node,
        FlowInstanceDO instance,
        Long modelId) {

        FlowNodeDTO next = node.getChildNode();

        moveToNextNodeOrFinish(next, instance, modelId);
    }

    public void handleConditionNode(
        FlowNodeDTO node,
        FlowInstanceDO instance,
        Long modelId) {

        Map<String, Object> vars = flowVariableService.loadVariables(instance.getId());

        FlowConditionNodeDTO matched = flowNodeRouter.matchConditionBranch(node.getConditionNodes(), vars);

        if (matched == null) {
            throw new BizException(FlowCodeEnum.FLOW_CONDITION_NO_MATCH);
        }

        FlowNodeDTO next = matched.getChildNode();

        moveToNextNodeOrFinish(next, instance, modelId);
    }

    public void handleApprovalNode(
        FlowNodeDTO node,
        FlowInstanceDO instance,
        Long modelId) {

        ApprovalTypeEnum type = node.getApprovalType() == null
            ? ApprovalTypeEnum.MANUAL_APPROVAL
            : node.getApprovalType();

        if (ApprovalTypeEnum.AUTO_PASS.equals(type)) {
            handleAutoPass(node, instance, modelId);

            return;
        }

        if (ApprovalTypeEnum.AUTO_REJECT.equals(type)) {
            flowRuntimeService.archiveInstance(instance, FlowInstanceStatusEnum.REJECTED);

            return;
        }

        List<FlowNodePlayerDTO> players = resolveNodePlayers(node, instance);

        if (CollUtil.isEmpty(players)) {
            throw new BizException(FlowCodeEnum.FLOW_NODE_PLAYER_EMPTY);
        }

        createTaskAndPlayers(node, instance, modelId, players);
    }

    public void handleAutoPass(
        FlowNodeDTO node,
        FlowInstanceDO instance,
        Long modelId) {

        Map<String, Object> vars = flowVariableService.loadVariables(instance.getId());

        FlowNodeDTO next = flowNodeRouter.resolveNextNode(node.getChildNode(), vars);

        moveToNextNodeOrFinish(next, instance, modelId);
    }

    public void createTaskAndPlayers(
        FlowNodeDTO node,
        FlowInstanceDO instance,
        Long modelId,
        List<FlowNodePlayerDTO> players) {

        String code = redissonService.serialNum().getSerialNumDate("FT");

        FlowTaskDO task = FlowTaskDO.builder()
            .code(code)
            .name(node.getName())
            .nodeCode(node.getCode())
            .instanceId(instance.getId())
            .modelId(modelId)
            .type(FlowTaskTypeEnum.MAJOR)
            .build();

        flowTaskRepository.save(task);

        FlowTaskPlayerTypeEnum playerType =
            node.getPlayerType() == null
                ? FlowTaskPlayerTypeEnum.USER
                : node.getPlayerType();

        List<FlowTaskPlayerDO> saves = new ArrayList<>(players.size());

        for (int i = 0; i < players.size(); i++) {

            FlowNodePlayerDTO p = players.get(i);

            saves.add(
                FlowTaskPlayerDO.builder()
                    .taskId(task.getId())
                    .instanceId(instance.getId())
                    .modelId(modelId)
                    .playerId(p.getId())
                    .playerName(p.getName())
                    .type(playerType)
                    .seq(i + 1)
                    .build()
            );
        }

        flowTaskPlayerRepository.saveBatch(saves, 1000);

        publishTaskCreatedEvent(task, players);
    }

    private FlowTaskPlayerDO getCurrentPlayer(
        List<FlowTaskPlayerDO> players,
        Long orgId,
        Long userId) {

        return players.stream()
            .filter(v -> isCurrentPlayer(v, orgId, userId))
            .findFirst()
            .orElseThrow(() ->
                new BizException(FlowCodeEnum.FLOW_NOT_PLAYER)
            );
    }

    private void moveInstanceToNode(
        FlowInstanceDO instance,
        FlowNodeDTO node) {

        instance.setCurrentNodeCode(node.getCode());
        instance.setCurrentNodeName(node.getName());

        flowInstanceRepository.updateById(instance);
    }

    public void moveToNextNodeOrFinish(
        FlowNodeDTO next,
        FlowInstanceDO instance,
        Long modelId) {

        if (next == null || FlowNodeTypeEnum.END.equals(next.getNodeType())) {
            flowRuntimeService.archiveInstance(instance, FlowInstanceStatusEnum.AUDITED);

            publishInstanceFinishedEvent(instance);

            return;
        }

        moveInstanceToNode(instance, next);

        createTasksForNode(next, instance, modelId);
    }

    private List<FlowNodePlayerDTO> resolveNodePlayers(
        FlowNodeDTO node,
        FlowInstanceDO instance) {

        List<FlowNodePlayerDTO> players = node.getNodePlayers();

        FlowAssigneeResolver resolver = assigneeResolverProvider.getIfAvailable();
        if (resolver == null) {
            return players;
        }

        Map<String, Object> vars = flowVariableService.loadVariables(instance.getId());

        List<FlowNodePlayerDTO> resolved =
            resolver.resolve(node, instance.getId(), vars);

        return CollUtil.isNotEmpty(resolved)
            ? resolved : players;
    }

    private FlowNodeDTO findNodeByModelId(
        Long modelId,
        String nodeCode) {

        FlowModelExtendDO modelExt = flowModelExtendRepository.getByModelId(modelId);

        if (modelExt == null || modelExt.getModelContent() == null) {
            throw new BizException(FlowCodeEnum.FLOW_NODE_NOT_FOUND);
        }

        FlowNodeDTO node =
            flowNodeRouter.findNodeByCode(
                modelExt.getModelContent().getNode(),
                nodeCode
            );

        if (node == null) {
            throw new BizException(FlowCodeEnum.FLOW_NODE_NOT_FOUND);
        }

        return node;
    }

    private boolean isCurrentPlayer(
        FlowTaskPlayerDO player,
        Long orgId,
        Long userId) {

        if (FlowTaskPlayerTypeEnum.USER.equals(player.getType())) {
            return userId.equals(player.getPlayerId());
        }

        FlowPlayerChecker checker = playerCheckerProvider.getIfAvailable();

        if (checker == null) {
            return false;
        }

        if (FlowTaskPlayerTypeEnum.ROLE.equals(player.getType())) {
            return checker.listRoleIdsByUserId(orgId, userId)
                .contains(player.getPlayerId());
        }

        if (FlowTaskPlayerTypeEnum.DEPARTMENT.equals(player.getType())) {
            return checker.listDepartmentIdsByUserId(orgId, userId)
                .contains(player.getPlayerId());
        }

        return false;
    }

    public void publishTaskApprovedEvent(
        FlowTaskDO task,
        FlowApproveTaskDTO entity) {

        flowEventService.publishTaskEvent(
            FlowTaskEventDTO.builder()
                .eventType(FlowTaskEventTypeEnum.TASK_APPROVED)
                .taskId(task.getId())
                .taskName(task.getName())
                .operatorId(entity.getCurrentUser().getUserId())
                .operatorName(entity.getCurrentUser().getRealName())
                .build()
        );
    }

    public void publishTaskRejectedEvent(
        FlowTaskDO task,
        FlowRejectTaskDTO entity) {

        flowEventService.publishTaskEvent(
            FlowTaskEventDTO.builder()
                .eventType(FlowTaskEventTypeEnum.TASK_REJECTED)
                .taskId(task.getId())
                .taskName(task.getName())
                .operatorId(entity.getCurrentUser().getUserId())
                .operatorName(entity.getCurrentUser().getRealName())
                .build()
        );
    }

    public void publishTaskCreatedEvent(
        FlowTaskDO task,
        List<FlowNodePlayerDTO> players) {

        flowEventService.publishTaskEvent(
            FlowTaskEventDTO.builder()
                .eventType(FlowTaskEventTypeEnum.TASK_CREATED)
                .taskId(task.getId())
                .taskName(task.getName())
                .assignees(players)
                .build()
        );
    }

    public void publishInstanceFinishedEvent(
        FlowInstanceDO instance) {

        flowEventService.publishInstanceEvent(
            FlowInstanceEventDTO.builder()
                .eventType(
                    FlowInstanceEventTypeEnum.INSTANCE_FINISHED
                )
                .instanceId(instance.getId())
                .instanceName(instance.getName())
                .instanceCode(instance.getCode())
                .bizNumber(instance.getBizNumber())
                .build()
        );
    }
}