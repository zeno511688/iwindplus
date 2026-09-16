/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.interfaces.controller.upms;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.application.query.upms.user.UserExtendBindGrantQueryService;
import com.iwindplus.mgt.application.query.upms.user.dto.UserExtendBindGrantSearchDTO;
import com.iwindplus.mgt.application.query.upms.user.vo.UserExtendBindGrantPageVO;
import com.iwindplus.mgt.application.query.upms.user.vo.UserExtendBindGrantVO;
import com.iwindplus.mgt.application.service.upms.user.UserExtendBindGrantApplicationService;
import com.iwindplus.mgt.application.service.upms.user.dto.UserExtendBindGrantUserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户扩展绑定授权相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "用户扩展绑定授权接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/userExtendBindGrant")
@Validated
@RequiredArgsConstructor
public class UserExtendBindGrantController extends BaseController {

    private final UserExtendBindGrantApplicationService userExtendBindGrantApplicationService;
    private final UserExtendBindGrantQueryService userExtendBindGrantQueryService;

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "批量删除")
    @DeleteMapping("removeByIds")
    @OperateValid(enabledGa = true)
    @OperateLog(bizType = "userExtendBindGrant", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除用户扩展绑定授权")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.userExtendBindGrantApplicationService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 绑定用户.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "绑定用户")
    @PutMapping("editUser")
    @OperateLog(keys = {"#entity.code",
        "#entity.mobile"}, bizType = "userExtendBindGrant", operateType = "editUser", operateName = "绑定用户", operateDesc = "用户扩展绑定用户")
    public ResultVO<Boolean> editUser(@RequestBody @Validated UserExtendBindGrantUserDTO entity) {
        boolean data = this.userExtendBindGrantApplicationService.editUser(entity);
        return ResultVO.success(data);
    }

    /**
     * 列表.
     *
     * @param entity 对象
     * @return ResultVO < IPage < UserExtendBindGrantPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<UserExtendBindGrantPageVO>> page(@Validated UserExtendBindGrantSearchDTO entity) {
        IPage<UserExtendBindGrantPageVO> data = this.userExtendBindGrantQueryService.page(entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO < UserExtendBindGrantVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<UserExtendBindGrantVO> getDetail(@RequestParam Long id) {
        UserExtendBindGrantVO data = this.userExtendBindGrantQueryService.getDetail(id);
        return ResultVO.success(data);
    }
}
