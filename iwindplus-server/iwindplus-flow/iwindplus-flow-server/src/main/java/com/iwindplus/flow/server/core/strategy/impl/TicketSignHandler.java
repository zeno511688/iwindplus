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
 * 票签（类似投票，达到比例即通过） 审批处理器.
 *
 * @author zengdegui
 * @since 2026/05/22 20:14
 */
@Slf4j
@Component
public class TicketSignHandler implements ApprovalHandler {

    @Override
    public ApprovalMethodEnum getType() {
        return ApprovalMethodEnum.TICKET_SIGN;
    }

    @Override
    public FlowApprovalResultVO approve(FlowTaskDO task, List<FlowTaskPlayerDO> players, FlowTaskPlayerDO current, FlowNodeDTO node,
        FlowApproveTaskDTO dto) {

        long remain = players.stream()
            .filter(p -> !p.getId().equals(current.getId()))
            .count();

        long total = players.size();

        long approved = total - remain;

        int passWeight = node.getPassWeight() != null
            ? node.getPassWeight()
            : 50;

        boolean done = approved * 100 >= total * passWeight;

        return FlowApprovalResultVO.builder()
            .approved(done)
            .recordIntermediate(!done)
            .removePlayerIds(List.of(current.getId()))
            .build();
    }
}
