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
import com.iwindplus.log.domain.dto.OperationLogDTO;
import com.iwindplus.log.domain.dto.OperationLogNewestDTO;
import com.iwindplus.log.domain.dto.OperationLogSearchDTO;
import com.iwindplus.log.domain.vo.OperationLogExtendVO;
import com.iwindplus.log.domain.vo.OperationLogPageVO;
import com.iwindplus.log.server.service.OperationLogService;
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
 * 操作日志相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2024/4/10
 */

@Tag(name = "操作日志接口")
@Slf4j
@RestController
@RequestMapping("admin/log/operation/log")
@Validated
@RequiredArgsConstructor
public class OperationLogController extends BaseController {

    private final OperationLogService operationLogService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @RedisIdempotent
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) OperationLogDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        entity.setUserId(this.getUserInfo().getUserId());
        boolean data = this.operationLogService.save(entity);
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
        boolean data = this.operationLogService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < OperationLogPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<OperationLogPageVO>> page(@Validated OperationLogSearchDTO entity) {
        IPage<OperationLogPageVO> data = this.operationLogService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO<OperationLogExtendVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<OperationLogExtendVO> getDetail(@RequestParam String id) {
        OperationLogExtendVO data = this.operationLogService.getDetail(id);
        return ResultVO.success(data);
    }

    /**
     * 根据条件获取最新数据.
     *
     * @param bizNumber     业务流水号
     * @param bizType       业务类型
     * @param operationType 操作类型
     * @param operationName 操作名称
     * @return ResultVO<OperationLogVO>
     */
    @Operation(summary = "根据条件获取最新数据")
    @GetMapping("getNewestByCondition")
    public ResultVO<OperationLogExtendVO> getNewestByCondition(@RequestParam String bizNumber, @RequestParam String bizType,
        @RequestParam String operationType, @RequestParam String operationName) {
        OperationLogNewestDTO entity = new OperationLogNewestDTO();
        entity.setBizNumber(bizNumber);
        entity.setBizType(bizType);
        entity.setOperateType(operationType);
        entity.setOperateName(operationName);
        entity.setCurrentUserId(this.getUserInfo().getUserId());
        entity.setOrgId(this.getUserInfo().getOrgId());
        OperationLogExtendVO data = this.operationLogService.getNewestByCondition(entity);
        return ResultVO.success(data);
    }
}
