/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.server.web.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.redis.domain.annotation.RedisIdempotent;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.log.domain.dto.BinlogAlertDTO;
import com.iwindplus.log.domain.dto.BinlogAlertSearchDTO;
import com.iwindplus.log.domain.vo.BinlogAlertPageVO;
import com.iwindplus.log.domain.vo.BinlogAlertVO;
import com.iwindplus.log.server.service.BinlogAlertService;
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
 * binlog告警相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */

@Tag(name = "binlog告警接口")
@Slf4j
@RestController
@RequestMapping("admin/log/binlog/alert")
@Validated
@RequiredArgsConstructor
public class BinlogAlertController extends BaseController {

    private final BinlogAlertService binlogAlertService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @RedisIdempotent
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) BinlogAlertDTO entity) {
        boolean data = this.binlogAlertService.save(entity);
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
        boolean data = this.binlogAlertService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < BinlogAlertPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<BinlogAlertPageVO>> page(@Validated BinlogAlertSearchDTO entity) {
        IPage<BinlogAlertPageVO> data = this.binlogAlertService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO<BinlogAlertVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<BinlogAlertVO> getDetail(@RequestParam String id) {
        BinlogAlertVO data = this.binlogAlertService.getDetail(id);
        return ResultVO.success(data);
    }

}
