/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.server.web.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.redis.domain.annotation.RedisIdempotent;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.log.domain.dto.MailLogDTO;
import com.iwindplus.log.domain.dto.MailLogSearchDTO;
import com.iwindplus.log.domain.vo.MailLogPageVO;
import com.iwindplus.log.domain.vo.MailLogVO;
import com.iwindplus.log.server.service.MailLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邮箱日志相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "邮箱日志接口")
@Slf4j
@RestController
@RequestMapping("admin/log/mail/log")
@Validated
@RequiredArgsConstructor
public class MailLogController extends BaseController {

    private final MailLogService mailLogService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO < String>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @RedisIdempotent
    public ResultVO<String> save(@RequestBody @Validated({SaveGroup.class}) MailLogDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        entity.setUserId(this.getUserInfo().getUserId());
        String data = this.mailLogService.save(entity);
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
        boolean data = this.mailLogService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "编辑")
    @PutMapping("edit")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) MailLogDTO entity) {
        boolean data = this.mailLogService.edit(entity);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < MailLogPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<MailLogPageVO>> page(@Validated MailLogSearchDTO entity) {
        IPage<MailLogPageVO> data = this.mailLogService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO<MailLogVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<MailLogVO> getDetail(@RequestParam String id) {
        MailLogVO data = this.mailLogService.getDetail(id);
        return ResultVO.success(data);
    }
}
