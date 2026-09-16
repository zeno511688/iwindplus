/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.engine.strategy.impl;

import com.iwindplus.flow.domain.engine.model.FlowApproveTaskDTO;
import com.iwindplus.flow.domain.engine.model.FlowNodeDTO;
import com.iwindplus.flow.common.enums.ApprovalMethodEnum;
import com.iwindplus.flow.domain.engine.model.FlowApprovalResultVO;
import com.iwindplus.flow.domain.engine.strategy.ApprovalHandler;
import com.iwindplus.flow.infrastructure.persistence.task.FlowTaskDO;
import com.iwindplus.flow.infrastructure.persistence.task.FlowTaskPlayerDO;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 或签（有一人审批通过即可） 审批处理器.
 *
 * @author zengdegui
 * @since 2026/05/22 20:14
 */
@Slf4j
@Component
public class OrSignHandler implements ApprovalHandler {

    @Override
    public ApprovalMethodEnum getType() {
        return ApprovalMethodEnum.OR_SIGN;
    }

    @Override
    public FlowApprovalResultVO approve(
        FlowTaskDO task,
        List<FlowTaskPlayerDO> players,
        FlowTaskPlayerDO current,
        FlowNodeDTO node,
        FlowApproveTaskDTO dto) {

        List<Long> removePlayerIds = players.stream()
            .map(FlowTaskPlayerDO::getId)
            .filter(Objects::nonNull)
            .toList();

        return FlowApprovalResultVO.builder()
            .approved(true)
            .removePlayerIds(removePlayerIds)
            .recordIntermediate(false)
            .build();
    }
}
