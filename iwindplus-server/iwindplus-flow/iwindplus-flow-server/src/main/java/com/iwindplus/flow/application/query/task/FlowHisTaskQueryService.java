/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.application.query.task;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.flow.application.query.task.dto.FlowHisTaskSearchDTO;
import com.iwindplus.flow.application.query.task.vo.FlowHisTaskPageVO;
import com.iwindplus.flow.infrastructure.persistence.task.FlowHisTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 历史流程任务查询业务层.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FlowHisTaskQueryService {

    private final FlowHisTaskRepository flowHisTaskRepository;

    /**
     * 审批记录分页查询.
     *
     * @param entity 查询条件
     * @return IPage<FlowHisTaskPageVO>
     */
    public IPage<FlowHisTaskPageVO> approvalRecordPage(FlowHisTaskSearchDTO entity) {
        PageDTO<FlowHisTaskPageVO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        return this.flowHisTaskRepository.getBaseMapper().selectApprovalRecordPage(page, entity);
    }
}
