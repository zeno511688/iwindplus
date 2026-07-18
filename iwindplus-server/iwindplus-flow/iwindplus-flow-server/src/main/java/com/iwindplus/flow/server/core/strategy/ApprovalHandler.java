/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core.strategy;

import com.iwindplus.flow.domain.dto.FlowApproveTaskDTO;
import com.iwindplus.flow.domain.dto.FlowNodeDTO;
import com.iwindplus.flow.domain.enums.ApprovalMethodEnum;
import com.iwindplus.flow.domain.vo.FlowApprovalResultVO;
import com.iwindplus.flow.server.dal.model.FlowTaskDO;
import com.iwindplus.flow.server.dal.model.FlowTaskPlayerDO;
import java.util.List;

/**
 * 审批处理器.
 *
 * @author zengdegui
 * @since 2026/05/22 19:44
 */
public interface ApprovalHandler {

    /**
     * 获取审批方式.
     *
     * @return ApprovalMethodEnum
     */
    ApprovalMethodEnum getType();

    /**
     * 审批.
     *
     * @param task    任务
     * @param players 审批人列表
     * @param current 当前审批人
     * @param node    节点
     * @param dto     审批参数
     * @return FlowApprovalResultVO
     */
    FlowApprovalResultVO approve(
        FlowTaskDO task,
        List<FlowTaskPlayerDO> players,
        FlowTaskPlayerDO current,
        FlowNodeDTO node,
        FlowApproveTaskDTO dto
    );
}
