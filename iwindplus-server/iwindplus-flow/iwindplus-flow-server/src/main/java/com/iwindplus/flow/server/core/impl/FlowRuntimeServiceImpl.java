/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iwindplus.flow.domain.enums.FlowInstanceStatusEnum;
import com.iwindplus.flow.domain.enums.FlowTaskStatusEnum;
import com.iwindplus.flow.server.core.FlowRuntimeService;
import com.iwindplus.flow.server.dal.model.FlowHisInstanceDO;
import com.iwindplus.flow.server.dal.model.FlowHisInstanceExtendDO;
import com.iwindplus.flow.server.dal.model.FlowHisTaskDO;
import com.iwindplus.flow.server.dal.model.FlowHisTaskPlayerDO;
import com.iwindplus.flow.server.dal.model.FlowInstanceDO;
import com.iwindplus.flow.server.dal.model.FlowInstanceExtendDO;
import com.iwindplus.flow.server.dal.model.FlowTaskDO;
import com.iwindplus.flow.server.dal.model.FlowTaskPlayerDO;
import com.iwindplus.flow.server.dal.repository.FlowHisInstanceExtendRepository;
import com.iwindplus.flow.server.dal.repository.FlowHisInstanceRepository;
import com.iwindplus.flow.server.dal.repository.FlowHisTaskPlayerRepository;
import com.iwindplus.flow.server.dal.repository.FlowHisTaskRepository;
import com.iwindplus.flow.server.dal.repository.FlowInstanceExtendRepository;
import com.iwindplus.flow.server.dal.repository.FlowInstanceRepository;
import com.iwindplus.flow.server.dal.repository.FlowTaskPlayerRepository;
import com.iwindplus.flow.server.dal.repository.FlowTaskRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 流程运行时服务实现类.
 * <p>
 * 负责流程实例和任务的生命周期管理，包括归档、状态变更等操作。
 * 所有方法均在事务保护下执行，确保数据一致性。
 *
 * @author zengdegui
 * @since 2026/05/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowRuntimeServiceImpl implements FlowRuntimeService {

    private final FlowTaskRepository flowTaskRepository;
    private final FlowTaskPlayerRepository flowTaskPlayerRepository;

    private final FlowHisTaskRepository flowHisTaskRepository;
    private final FlowHisTaskPlayerRepository flowHisTaskPlayerRepository;

    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowInstanceExtendRepository flowInstanceExtendRepository;

    private final FlowHisInstanceRepository flowHisInstanceRepository;
    private final FlowHisInstanceExtendRepository flowHisInstanceExtendRepository;

    @Override
    public FlowInstanceDO getInstance(Long instanceId) {
        return flowInstanceRepository.getById(instanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveInstance(
        FlowInstanceDO instance,
        FlowInstanceStatusEnum status) {

        if (instance == null || instance.getId() == null) {
            return;
        }

        // 计算流程耗时
        long takeTime = instance.getCreatedTime() != null
            ? Duration.between(instance.getCreatedTime(), LocalDateTime.now()).toMillis()
            : 0L;

        // 创建历史实例记录
        FlowHisInstanceDO hisInstance = BeanUtil.copyProperties(instance, FlowHisInstanceDO.class);
        hisInstance.setStatus(status);
        hisInstance.setTakeTime(takeTime);
        flowHisInstanceRepository.save(hisInstance);

        // 迁移实例扩展数据到历史表
        FlowInstanceExtendDO extend = flowInstanceExtendRepository.getOne(
            Wrappers.lambdaQuery(FlowInstanceExtendDO.class)
                .eq(FlowInstanceExtendDO::getInstanceId, instance.getId())
        );

        if (extend != null) {
            FlowHisInstanceExtendDO hisExtend = BeanUtil.copyProperties(extend, FlowHisInstanceExtendDO.class);
            flowHisInstanceExtendRepository.save(hisExtend);
            flowInstanceExtendRepository.removeById(extend.getId());
        }

        // 删除运行中实例
        flowInstanceRepository.removeById(instance.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveTasks(
        List<FlowTaskDO> tasks,
        FlowTaskStatusEnum status,
        String comment,
        List<Long> removePlayerIds) {

        if (CollUtil.isEmpty(tasks)) {
            return;
        }

        // 1. 收集所有任务ID
        List<Long> taskIds = tasks.stream()
            .map(FlowTaskDO::getId)
            .filter(Objects::nonNull)
            .toList();

        // 2. 确定需要删除的审批人
        List<FlowTaskPlayerDO> playersToRemove = determinePlayersToRemove(taskIds, removePlayerIds);

        // 3. 归档任务到历史表
        archiveTasksToHistory(tasks, status, comment);

        // 4. 归档并删除审批人
        archiveAndRemovePlayers(playersToRemove);

        // 5. 删除原任务
        flowTaskRepository.removeByIds(taskIds);
    }

    /**
     * 确定需要删除的审批人列表.
     */
    private List<FlowTaskPlayerDO> determinePlayersToRemove(List<Long> taskIds, List<Long> removePlayerIds) {
        if (CollUtil.isEmpty(removePlayerIds)) {
            // 未指定ID时，删除所有关联的审批人
            return flowTaskPlayerRepository.list(
                Wrappers.lambdaQuery(FlowTaskPlayerDO.class)
                    .in(FlowTaskPlayerDO::getTaskId, taskIds)
            );
        } else {
            // 只删除指定的审批人
            return flowTaskPlayerRepository.listByIds(removePlayerIds);
        }
    }

    /**
     * 归档任务到历史表.
     */
    private void archiveTasksToHistory(List<FlowTaskDO> tasks, FlowTaskStatusEnum status, String comment) {
        List<FlowHisTaskDO> hisTasks = new ArrayList<>(tasks.size());
        LocalDateTime now = LocalDateTime.now();

        for (FlowTaskDO task : tasks) {
            FlowHisTaskDO hisTask = BeanUtil.copyProperties(task, FlowHisTaskDO.class);
            hisTask.setStatus(status);
            
            // 计算任务耗时
            long takeTime = task.getCreatedTime() != null
                ? Duration.between(task.getCreatedTime(), now).toMillis()
                : 0L;
            hisTask.setTakeTime(takeTime);
            hisTask.setComment(comment);
            hisTasks.add(hisTask);
        }
        
        flowHisTaskRepository.saveBatch(hisTasks, 1000);
    }

    /**
     * 归档审批人并从原表删除.
     */
    private void archiveAndRemovePlayers(List<FlowTaskPlayerDO> playersToRemove) {
        if (CollUtil.isNotEmpty(playersToRemove)) {
            // 归档到历史表
            List<FlowHisTaskPlayerDO> hisPlayers = playersToRemove.stream()
                .map(p -> BeanUtil.copyProperties(p, FlowHisTaskPlayerDO.class))
                .toList();
            flowHisTaskPlayerRepository.saveBatch(hisPlayers, 1000);

            // 删除原审批人
            List<Long> playerIds = playersToRemove.stream()
                .map(FlowTaskPlayerDO::getId)
                .filter(Objects::nonNull)
                .toList();
            flowTaskPlayerRepository.removeByIds(playerIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveActiveTasksForInstance(
        Long instanceId,
        FlowTaskStatusEnum status) {

        List<FlowTaskDO> tasks = flowTaskRepository.list(
            Wrappers.lambdaQuery(FlowTaskDO.class)
                .eq(FlowTaskDO::getInstanceId, instanceId)
        );

        archiveTasks(tasks, status, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveIntermediateApprovalRecord(
        FlowTaskDO task,
        FlowTaskPlayerDO player,
        String comment) {

        if (task == null || player == null) {
            return;
        }

        // 计算耗时
        long takeTime = task.getCreatedTime() != null
            ? Duration.between(task.getCreatedTime(), LocalDateTime.now()).toMillis()
            : 0L;

        // 归档任务为中间审批状态
        FlowHisTaskDO hisTask = BeanUtil.copyProperties(task, FlowHisTaskDO.class);
        hisTask.setStatus(FlowTaskStatusEnum.APPROVAL);
        hisTask.setTakeTime(takeTime);
        hisTask.setComment(comment);
        flowHisTaskRepository.save(hisTask);

        // 归档审批人
        FlowHisTaskPlayerDO hisPlayer = BeanUtil.copyProperties(player, FlowHisTaskPlayerDO.class);
        hisPlayer.setTaskId(hisTask.getId());
        flowHisTaskPlayerRepository.save(hisPlayer);
    }
}