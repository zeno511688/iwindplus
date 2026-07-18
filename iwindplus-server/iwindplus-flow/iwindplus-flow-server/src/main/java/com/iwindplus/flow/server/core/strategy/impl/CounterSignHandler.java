/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core.strategy.impl;

import com.iwindplus.flow.domain.dto.FlowApproveTaskDTO;
import com.iwindplus.flow.domain.dto.FlowNodeDTO;
import com.iwindplus.flow.domain.enums.ApprovalMethodEnum;
import com.iwindplus.flow.domain.vo.FlowApprovalResultVO;
import com.iwindplus.flow.server.core.strategy.ApprovalHandler;
import com.iwindplus.flow.server.dal.model.FlowTaskDO;
import com.iwindplus.flow.server.dal.model.FlowTaskPlayerDO;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 会签（可同时审批，所以人必须审批通过） 审批处理器.
 *
 * @author zengdegui
 * @since 2026/05/22 20:14
 */
@Slf4j
@Component
public class CounterSignHandler implements ApprovalHandler {

    @Override
    public ApprovalMethodEnum getType() {
        return ApprovalMethodEnum.COUNTER_SIGN;
    }

    @Override
    public FlowApprovalResultVO approve(
        FlowTaskDO task,
        List<FlowTaskPlayerDO> players,
        FlowTaskPlayerDO current,
        FlowNodeDTO node,
        FlowApproveTaskDTO dto) {

        // 剩余审批人数
        long remain = players.stream()
            .filter(p -> !p.getId().equals(current.getId()))
            .count();

        boolean approved = remain == 0;
        return FlowApprovalResultVO.builder()
            .approved(approved)
            .removePlayerIds(List.of(current.getId()))
            .recordIntermediate(!approved)
            .build();
    }
}
