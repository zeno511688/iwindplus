/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.web.admin.power;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.redis.domain.annotation.RedisIdempotent;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.domain.dto.power.UserExtendYubikeyEditDTO;
import com.iwindplus.mgt.domain.dto.power.UserExtendYubikeySaveDTO;
import com.iwindplus.mgt.domain.dto.power.UserExtendYubikeySearchDTO;
import com.iwindplus.mgt.domain.enums.YubikeyBizTypeEnum;
import com.iwindplus.mgt.domain.vo.power.UserExtendYubikeyPageVO;
import com.iwindplus.mgt.domain.vo.power.UserExtendYubikeyVO;
import com.iwindplus.mgt.server.service.power.UserExtendYubikeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
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
 * 用户扩展yubikey相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "用户扩展yubikey接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/userExtendYubikey")
@Validated
@RequiredArgsConstructor
public class UserExtendYubikeyController extends BaseController {

    private final UserExtendYubikeyService userExtendYubikeyService;

    /**
     * 添加.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "添加")
    @PostMapping("save")
    @OperateLog(keys = "#entity.id", bizType = "userExtendYubikey", operateType = "save", operateName = "添加", operateDesc = "添加系统")
    public ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) UserExtendYubikeySaveDTO entity) {
        boolean data = this.userExtendYubikeyService.save(entity);
        return ResultVO.success(data);
    }

    /**
     * 批量删除.
     *
     * @param ids 主键集合
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "批量删除")
    @DeleteMapping("removeByIds")
    @OperateValid(enabledGa = true)
    @OperateLog(bizType = "userExtendYubikey", operateType = "removeByIds", operateName = "批量删除", operateDesc = "批量删除用户扩展yubikey")
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.userExtendYubikeyService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 编辑.
     *
     * @param entity 对象
     * @return ResultVO < Boolean>
     */
    @Operation(summary = "编辑用户扩展yubikey")
    @PutMapping("edit")
    @RedisIdempotent
    @OperateLog(keys = "#entity.id", bizType = "userExtendYubikey", operateType = "edit", operateName = "编辑", operateDesc = "编辑用户扩展yubikey")
    public ResultVO<Boolean> edit(@RequestBody @Validated({EditGroup.class}) UserExtendYubikeyEditDTO entity) {
        boolean data = this.userExtendYubikeyService.edit(entity);
        return ResultVO.success(data);
    }

    /**
     * 用户的yubikey.
     *
     * @param userId  用户主键
     * @param bizType 业务类型
     * @return ResultVO < List<UserYubikeyVO>>
     */
    @Operation(summary = "用户的yubikey")
    @GetMapping("getByUserId")
    public ResultVO<UserExtendYubikeyVO> getByUserId(
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) YubikeyBizTypeEnum bizType) {
        userId = Optional.ofNullable(userId).orElse(this.getUserInfo().getUserId());
        bizType = Optional.ofNullable(bizType).orElse(YubikeyBizTypeEnum.GENERAL);
        UserExtendYubikeyVO data = this.userExtendYubikeyService.getByUserId(userId, bizType);
        return ResultVO.success(data);
    }

    /**
     * 用户的yubikey列表.
     *
     * @param entity 对象
     * @return ResultVO < List<UserYubikeyVO>>
     */
    @Operation(summary = "用户的yubikey列表")
    @GetMapping("pageByUserId")
    public ResultVO<IPage<UserExtendYubikeyPageVO>> pageByUserId(@Validated UserExtendYubikeySearchDTO entity) {
        IPage<UserExtendYubikeyPageVO> data = this.userExtendYubikeyService.pageByUserId(entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id 主键
     * @return ResultVO<UserYubikeyVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<UserExtendYubikeyVO> getDetail(@RequestParam Long id) {
        UserExtendYubikeyVO data = this.userExtendYubikeyService.getDetail(id);
        return ResultVO.success(data);
    }
}
