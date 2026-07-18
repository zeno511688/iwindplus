/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core;

import com.iwindplus.flow.domain.dto.FlowAddTaskPlayerDTO;
import com.iwindplus.flow.domain.dto.FlowApproveTaskDTO;
import com.iwindplus.flow.domain.dto.FlowDelegateTaskDTO;
import com.iwindplus.flow.domain.dto.FlowJumpTaskDTO;
import com.iwindplus.flow.domain.dto.FlowNodeDTO;
import com.iwindplus.flow.domain.dto.FlowRejectTaskDTO;
import com.iwindplus.flow.domain.dto.FlowRemoveTaskPlayerDTO;
import com.iwindplus.flow.domain.dto.FlowTransferTaskDTO;
import com.iwindplus.flow.server.dal.model.FlowInstanceDO;

/**
 * 流程任务动作业务层接口.
 *
 * @author zengdegui
 * @since 2026/05/22 22:49
 */
public interface FlowTaskActionService {

    /**
     * 审批通过任务.
     *
     * @param entity 审批参数
     * @return true=节点已完成并推进（或流程结束），false=等待其他人审批
     */
    boolean approveTask(FlowApproveTaskDTO entity);

    /**
     * 驳回任务.
     *
     * @param entity 驳回参数
     * @return boolean
     */
    boolean rejectTask(FlowRejectTaskDTO entity);

    /**
     * 转交任务.
     *
     * @param entity 转交参数
     * @return boolean
     */
    boolean transferTask(FlowTransferTaskDTO entity);

    /**
     * 加签（增加任务处理人）.
     *
     * @param entity 加签参数
     * @return boolean
     */
    boolean addTaskPlayer(FlowAddTaskPlayerDTO entity);

    /**
     * 减签（移除任务处理人）.
     *
     * @param entity 减签参数
     * @return boolean
     */
    boolean removeTaskPlayer(FlowRemoveTaskPlayerDTO entity);

    /**
     * 流程跳转（跳转到指定节点）.
     *
     * @param entity 跳转参数
     * @return boolean
     */
    boolean jumpTask(FlowJumpTaskDTO entity);

    /**
     * 委托任务（将任务委托给其他人代办）.
     *
     * @param entity 委托参数
     * @return boolean
     */
    boolean delegateTask(FlowDelegateTaskDTO entity);

    /**
     * 创建节点任务.
     *
     * @param node     节点
     * @param instance 实例
     * @param modelId  模型ID
     */
    void createTasksForNode(
        FlowNodeDTO node,
        FlowInstanceDO instance,
        Long modelId);
}
