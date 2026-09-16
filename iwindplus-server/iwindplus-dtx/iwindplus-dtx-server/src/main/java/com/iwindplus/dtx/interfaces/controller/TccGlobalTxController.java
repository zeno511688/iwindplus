/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.dtx.application.query.TccGlobalTxQueryService;
import com.iwindplus.dtx.application.query.dto.TccGlobalTxSearchDTO;
import com.iwindplus.dtx.application.query.vo.TccGlobalTxPageVO;
import com.iwindplus.dtx.application.query.vo.TccGlobalTxVO;
import com.iwindplus.dtx.application.service.TccGlobalTxApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * tcc全局事务相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "tcc全局事务接口")
@Slf4j
@RestController
@RequestMapping("admin/dtx/globalTx")
@Validated
@RequiredArgsConstructor
public class TccGlobalTxController extends BaseController {

    private final TccGlobalTxQueryService tccGlobalTxQueryService;
    private final TccGlobalTxApplicationService tccGlobalTxApplicationService;

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "批量删除")
    @DeleteMapping("removeByIds")
    @OperateValid(enabledGa = true)
    @OperateLog(bizType = "globalTx", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除API白名单")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.tccGlobalTxApplicationService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < TccGlobalTxPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<TccGlobalTxPageVO>> page(@Validated TccGlobalTxSearchDTO entity) {
        IPage<TccGlobalTxPageVO> data = this.tccGlobalTxQueryService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO<TccGlobalTxVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<TccGlobalTxVO> getDetail(@RequestParam Long id) {
        TccGlobalTxVO data = this.tccGlobalTxQueryService.getDetail(id);
        return ResultVO.success(data);
    }
}
