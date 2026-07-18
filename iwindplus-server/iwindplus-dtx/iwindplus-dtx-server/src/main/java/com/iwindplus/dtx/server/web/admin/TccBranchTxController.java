/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.web.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.dtx.domain.dto.TccBranchTxSearchDTO;
import com.iwindplus.dtx.domain.vo.TccBranchTxPageVO;
import com.iwindplus.dtx.domain.vo.TccBranchTxVO;
import com.iwindplus.dtx.server.service.TccBranchTxService;
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
 * tcc分支事务相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "tcc分支事务接口")
@Slf4j
@RestController
@RequestMapping("admin/dtx/branchTx")
@Validated
@RequiredArgsConstructor
public class TccBranchTxController extends BaseController {

    private final TccBranchTxService tccBranchTxService;

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "批量删除")
    @DeleteMapping("removeByIds")
    @OperateValid(enabledGa = true)
    @OperateLog(bizType = "branchTx", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除API白名单")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.tccBranchTxService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < TccBranchTxPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<TccBranchTxPageVO>> page(@Validated TccBranchTxSearchDTO entity) {
        IPage<TccBranchTxPageVO> data = this.tccBranchTxService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO<TccBranchTxVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<TccBranchTxVO> getDetail(@RequestParam Long id) {
        TccBranchTxVO data = this.tccBranchTxService.getDetail(id);
        return ResultVO.success(data);
    }
}
