/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.flow.domain.dto.FlowApprovalRecordSearchDTO;
import com.iwindplus.flow.domain.vo.FlowHisTaskPageVO;
import com.iwindplus.flow.server.dal.repository.FlowHisTaskRepository;
import com.iwindplus.flow.server.service.FlowHisTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 历史流程任务业务层接口实现类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class FlowHisTaskServiceImpl implements FlowHisTaskService {

    private final FlowHisTaskRepository flowHisTaskRepository;

    @Override
    public IPage<FlowHisTaskPageVO> approvalRecordPage(FlowApprovalRecordSearchDTO entity) {
        PageDTO<FlowHisTaskPageVO> page = new PageDTO<>(entity.getCurrent(), entity.getSize());
        page.setOptimizeCountSql(Boolean.FALSE);
        page.setOptimizeJoinOfCountSql(Boolean.FALSE);
        return this.flowHisTaskRepository.getBaseMapper().selectApprovalRecordPage(page, entity);
    }
}
