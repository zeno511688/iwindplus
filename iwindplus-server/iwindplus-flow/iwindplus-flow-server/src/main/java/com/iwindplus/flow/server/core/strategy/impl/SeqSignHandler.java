/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.core.strategy.impl;

import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.flow.domain.dto.FlowApproveTaskDTO;
import com.iwindplus.flow.domain.dto.FlowNodeDTO;
import com.iwindplus.flow.domain.enums.ApprovalMethodEnum;
import com.iwindplus.flow.domain.enums.FlowCodeEnum;
import com.iwindplus.flow.domain.vo.FlowApprovalResultVO;
import com.iwindplus.flow.server.core.strategy.ApprovalHandler;
import com.iwindplus.flow.server.dal.model.FlowTaskDO;
import com.iwindplus.flow.server.dal.model.FlowTaskPlayerDO;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 按顺序依次审批处理器.
 *
 * @author zengdegui
 * @since 2026/05/22 20:06
 */
@Slf4j
@Component
public class SeqSignHandler implements ApprovalHandler {

    @Override
    public ApprovalMethodEnum getType() {
        return ApprovalMethodEnum.SEQ_SIGN;
    }

    @Override
    public FlowApprovalResultVO approve(
        FlowTaskDO task,
        List<FlowTaskPlayerDO> players,
        FlowTaskPlayerDO current,
        FlowNodeDTO node,
        FlowApproveTaskDTO dto) {

        int minSeq = players.stream()
            .mapToInt(p -> p.getSeq() == null ? 0 : p.getSeq())
            .min()
            .orElse(0);

        if (!Integer.valueOf(minSeq).equals(current.getSeq())) {
            throw new BizException(FlowCodeEnum.FLOW_NOT_PLAYER);
        }

        long remain = players.stream()
            .filter(p -> !p.getId().equals(current.getId()))
            .count();

        return FlowApprovalResultVO.builder()
            .approved(remain == 0)
            .recordIntermediate(remain > 0)
            .removePlayerIds(List.of(current.getId()))
            .build();
    }
}
