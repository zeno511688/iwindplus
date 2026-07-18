/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.flow.domain.dto.FlowApprovalRecordSearchDTO;
import com.iwindplus.flow.domain.vo.FlowHisTaskPageVO;

/**
 * 历史流程任务业务层接口类.
 *
 * @author zengdegui
 * @since 2024/11/03 19:11
 */
public interface FlowHisTaskService {

    /**
     * 审批记录分页查询.
     *
     * @param entity 查询条件
     * @return IPage<FlowHisTaskPageVO>
     */
    IPage<FlowHisTaskPageVO> approvalRecordPage(FlowApprovalRecordSearchDTO entity);
}
