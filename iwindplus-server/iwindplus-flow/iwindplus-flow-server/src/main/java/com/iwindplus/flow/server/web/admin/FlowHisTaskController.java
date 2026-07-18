/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.web.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.flow.domain.dto.FlowApprovalRecordSearchDTO;
import com.iwindplus.flow.domain.vo.FlowHisTaskPageVO;
import com.iwindplus.flow.server.service.FlowHisTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 历史流程任务相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2026/05/20
 */
@Tag(name = "历史流程任务接口")
@Slf4j
@RestController
@RequestMapping("admin/flow/hisTask")
@Validated
@RequiredArgsConstructor
public class FlowHisTaskController extends BaseController {

    private final FlowHisTaskService flowHisTaskService;

    /**
     * 审批记录列表.
     *
     * @param entity 查询条件
     * @return ResultVO<IPage < FlowHisTaskPageVO>>
     */
    @Operation(summary = "审批记录列表")
    @GetMapping("approvalRecord")
    public ResultVO<IPage<FlowHisTaskPageVO>> approvalRecord(@Validated FlowApprovalRecordSearchDTO entity) {
        if (Boolean.TRUE.equals(entity.getOnlyMine())) {
            entity.setUserId(this.getUserInfo().getUserId());
        }
        IPage<FlowHisTaskPageVO> data = this.flowHisTaskService.approvalRecordPage(entity);
        return ResultVO.success(data);
    }
}
