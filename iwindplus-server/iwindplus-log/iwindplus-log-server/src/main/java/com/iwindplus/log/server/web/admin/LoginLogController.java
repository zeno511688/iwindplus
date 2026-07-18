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
import com.iwindplus.log.domain.dto.LoginLogDTO;
import com.iwindplus.log.domain.dto.LoginLogSearchDTO;
import com.iwindplus.log.domain.vo.LoginLogExtendVO;
import com.iwindplus.log.domain.vo.LoginLogPageVO;
import com.iwindplus.log.domain.vo.LoginLogVO;
import com.iwindplus.log.server.service.LoginLogService;
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
 * 登录日志相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */

@Tag(name = "登录日志接口")
@Slf4j
@RestController
@RequestMapping("admin/log/login/log")
@Validated
@RequiredArgsConstructor
public class LoginLogController extends BaseController {

    private final LoginLogService loginLogService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @RedisIdempotent
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) LoginLogDTO entity) {
        entity.setOrgId(this.getUserInfo().getOrgId());
        entity.setUserId(this.getUserInfo().getUserId());
        boolean data = this.loginLogService.saveBatch(List.of(entity));
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
        boolean data = this.loginLogService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < LoginLogPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<LoginLogPageVO>> page(@Validated LoginLogSearchDTO entity) {
        IPage<LoginLogPageVO> data = this.loginLogService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO<LoginLogExtendVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<LoginLogExtendVO> getDetail(@RequestParam String id) {
        LoginLogExtendVO data = this.loginLogService.getDetail(id);
        return ResultVO.success(data);
    }

    /**
     * 获取最新登录信息.
     *
     * @return ResultVO<LoginLogVO>
     */
    @Operation(summary = "获取最新登录信息")
    @GetMapping("getLoginInfo")
    public ResultVO<LoginLogVO> getLoginInfo() {
        Long userId = this.getUserInfo().getUserId();
        Long orgId = this.getUserInfo().getOrgId();
        LoginLogVO data = this.loginLogService.getLoginInfo(userId, orgId);
        return ResultVO.success(data);
    }
}
