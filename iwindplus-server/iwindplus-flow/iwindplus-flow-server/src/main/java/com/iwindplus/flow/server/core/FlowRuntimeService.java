/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core;

import com.iwindplus.flow.domain.enums.FlowInstanceStatusEnum;
import com.iwindplus.flow.domain.enums.FlowTaskStatusEnum;
import com.iwindplus.flow.server.dal.model.FlowInstanceDO;
import com.iwindplus.flow.server.dal.model.FlowTaskDO;
import com.iwindplus.flow.server.dal.model.FlowTaskPlayerDO;
import java.util.List;

/**
 * 流程运行业务层接口.
 *
 * @author zengdegui
 * @since 2026/05/22 23:40
 */
public interface FlowRuntimeService {

    /**
     * 获取运行中实例.
     *
     * @param instanceId 实例ID
     * @return 实例
     */
    FlowInstanceDO getInstance(Long instanceId);

    /**
     * 归档流程实例.
     *
     * @param instance 实例
     * @param status   状态
     */
    void archiveInstance(
        FlowInstanceDO instance,
        FlowInstanceStatusEnum status
    );

    /**
     * 归档任务.
     *
     * @param tasks           任务集合
     * @param status          状态
     * @param comment         审批意见
     * @param removePlayerIds 需要删除的审批人ID列表（为空则删除所有）
     */
    void archiveTasks(
        List<FlowTaskDO> tasks,
        FlowTaskStatusEnum status,
        String comment,
        List<Long> removePlayerIds
    );

    /**
     * 归档实例下所有活动任务.
     *
     * @param instanceId 实例ID
     * @param status     状态
     */
    void archiveActiveTasksForInstance(
        Long instanceId,
        FlowTaskStatusEnum status
    );

    /**
     * 保存中间审批记录.
     *
     * @param task    任务
     * @param player  当前审批人
     * @param comment 审批意见
     */
    void saveIntermediateApprovalRecord(
        FlowTaskDO task,
        FlowTaskPlayerDO player,
        String comment
    );
}
