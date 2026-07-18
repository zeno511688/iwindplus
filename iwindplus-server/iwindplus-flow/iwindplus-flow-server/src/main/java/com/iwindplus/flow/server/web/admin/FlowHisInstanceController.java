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
import com.iwindplus.flow.domain.dto.FlowHisInstanceSearchDTO;
import com.iwindplus.flow.domain.vo.FlowHisInstancePageVO;
import com.iwindplus.flow.server.service.FlowHisInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 历史流程实例相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2026/05/20
 */
@Tag(name = "历史流程实例接口")
@Slf4j
@RestController
@RequestMapping("admin/flow/hisInstance")
@Validated
@RequiredArgsConstructor
public class FlowHisInstanceController extends BaseController {

    private final FlowHisInstanceService flowHisInstanceService;

    /**
     * 我的发起列表.
     *
     * @param entity 查询条件
     * @return ResultVO<IPage < FlowHisInstancePageVO>>
     */
    @Operation(summary = "我的发起列表")
    @GetMapping("myInitiated")
    public ResultVO<IPage<FlowHisInstancePageVO>> myInitiated(@Validated FlowHisInstanceSearchDTO entity) {
        entity.setUserId(this.getUserInfo().getUserId());
        IPage<FlowHisInstancePageVO> data = this.flowHisInstanceService.myInitiatedPage(entity);
        return ResultVO.success(data);
    }

    /**
     * 我的已办列表.
     *
     * @param entity 查询条件
     * @return ResultVO<IPage < FlowHisInstancePageVO>>
     */
    @Operation(summary = "我的已办列表")
    @GetMapping("myDone")
    public ResultVO<IPage<FlowHisInstancePageVO>> myDone(@Validated FlowHisInstanceSearchDTO entity) {
        entity.setUserId(this.getUserInfo().getUserId());
        IPage<FlowHisInstancePageVO> data = this.flowHisInstanceService.myDonePage(entity);
        return ResultVO.success(data);
    }

    /**
     * 抄送我的列表.
     *
     * @param entity 查询条件
     * @return ResultVO<IPage < FlowHisInstancePageVO>>
     */
    @Operation(summary = "抄送我的列表")
    @GetMapping("myCc")
    public ResultVO<IPage<FlowHisInstancePageVO>> myCc(@Validated FlowHisInstanceSearchDTO entity) {
        entity.setUserId(this.getUserInfo().getUserId());
        IPage<FlowHisInstancePageVO> data = this.flowHisInstanceService.myCcPage(entity);
        return ResultVO.success(data);
    }

    /**
     * 所有流程列表.
     *
     * @param entity 查询条件
     * @return ResultVO<IPage < FlowHisInstancePageVO>>
     */
    @Operation(summary = "所有流程列表")
    @GetMapping("all")
    public ResultVO<IPage<FlowHisInstancePageVO>> all(@Validated FlowHisInstanceSearchDTO entity) {
        IPage<FlowHisInstancePageVO> data = this.flowHisInstanceService.allPage(entity);
        return ResultVO.success(data);
    }
}