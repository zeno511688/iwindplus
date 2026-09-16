/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.es.domain.dto.EsPageDTO;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.log.api.dto.GatewayLogDTO;
import com.iwindplus.log.application.query.GatewayLogQueryService;
import com.iwindplus.log.application.query.dto.GatewayLogSearchAfterDTO;
import com.iwindplus.log.application.query.dto.GatewayLogSearchDTO;
import com.iwindplus.log.application.query.vo.GatewayLogExtendVO;
import com.iwindplus.log.application.query.vo.GatewayLogPageVO;
import com.iwindplus.log.application.service.GatewayLogApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 网关日志相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "网关日志接口")
@Slf4j
@RestController
@RequestMapping("admin/log/gateway/log")
@Validated
@RequiredArgsConstructor
public class GatewayLogController extends BaseController {

    private final GatewayLogApplicationService gatewayLogApplicationService;
    private final GatewayLogQueryService gatewayLogQueryService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) GatewayLogDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        entity.setUserId(this.getUserInfo().getUserId());
        boolean data = this.gatewayLogApplicationService.saveBatch(List.of(entity));
        return ResultVO.success(data);
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "批量删除")
    @DeleteMapping("removeByIds")
    @OperateValid(enabledGa = true)
    public ResultVO<Boolean> removeByIds(@RequestParam List<String> ids) {
        boolean data = this.gatewayLogApplicationService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < GatewayLogPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<GatewayLogPageVO>> page(@Validated GatewayLogSearchDTO entity) {
        IPage<GatewayLogPageVO> data = this.gatewayLogQueryService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 列表(深分页).
     *
     * @param entity 对象
     * @return ResultVO<EsPageDTO < GatewayLogPageVO>>
     */
    @Operation(summary = "列表(深分页)")
    @GetMapping("pageByAfter")
    public ResultVO<EsPageDTO<GatewayLogPageVO>> pageByAfter(@Validated GatewayLogSearchAfterDTO entity) {
        EsPageDTO<GatewayLogPageVO> data = this.gatewayLogQueryService.pageByAfter(entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO<GatewayLogExtendVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<GatewayLogExtendVO> getDetail(@RequestParam String id) {
        GatewayLogExtendVO data = this.gatewayLogQueryService.getDetail(id);
        return ResultVO.success(data);
    }
}
