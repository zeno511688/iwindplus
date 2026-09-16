/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.flow.application.query.task.FlowTaskQueryService;
import com.iwindplus.flow.application.query.task.dto.FlowTaskSearchDTO;
import com.iwindplus.flow.application.query.task.vo.FlowTaskPageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流程任务相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2026/05/20
 */
@Tag(name = "流程任务接口")
@Slf4j
@RestController
@RequestMapping("admin/flow/task")
@Validated
@RequiredArgsConstructor
public class FlowTaskController extends BaseController {

    private final FlowTaskQueryService flowTaskQueryService;

    /**
     * 我的待办列表.
     *
     * @param entity 查询条件
     * @return ResultVO<IPage < FlowTaskPageVO>>
     */
    @Operation(summary = "我的待办列表")
    @GetMapping("myPending")
    public ResultVO<IPage<FlowTaskPageVO>> myPending(@Validated FlowTaskSearchDTO entity) {
        entity.setUserId(this.getUserInfo().getUserId());
        IPage<FlowTaskPageVO> data = this.flowTaskQueryService.myPendingPage(entity);
        return ResultVO.success(data);
    }
}