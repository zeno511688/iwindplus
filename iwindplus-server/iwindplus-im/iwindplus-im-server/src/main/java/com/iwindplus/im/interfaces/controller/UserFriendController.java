/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.operate.domain.annotation.OperateLog;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.im.application.query.UserFriendQueryService;
import com.iwindplus.im.application.query.dto.UserFriendSearchDTO;
import com.iwindplus.im.application.query.vo.UserFriendPageVO;
import com.iwindplus.im.application.service.UserFriendApplicationService;
import com.iwindplus.im.application.service.dto.UserFriendDTO;
import com.iwindplus.im.common.enums.FriendStatusEnum;
import com.iwindplus.im.infrastructure.persistence.mysql.UserFriendDO;
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
 * 用户好友相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "用户好友接口")
@Slf4j
@RestController
@RequestMapping("admin/im/user/friend")
@Validated
@RequiredArgsConstructor
public class UserFriendController extends BaseController {

    private final UserFriendQueryService userFriendQueryService;
    private final UserFriendApplicationService userFriendApplicationService;

    /**
     * 添加好友.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "添加好友")
    @PostMapping("save")
    @OperateLog(keys = "#entity.id", bizType = "userFriend", operateType = "save", operateName = "添加", operateDesc = "添加好友")
    public ResultVO<Boolean> save(@RequestBody @Validated(SaveGroup.class) UserFriendDTO entity) {
        final UserBaseVO userInfo = this.getUserInfo();
        boolean data = this.userFriendApplicationService.save(entity, userInfo);
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
    public ResultVO<Boolean> removeByIds(@RequestParam List<Long> ids) {
        boolean data = this.userFriendApplicationService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 编辑状态.
     *
     * @param id     主键
     * @param status 状态
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "编辑状态")
    @PutMapping("editStatus")
    public ResultVO<Boolean> editStatus(@RequestParam Long id, @RequestParam FriendStatusEnum status) {
        boolean data = this.userFriendApplicationService.editStatus(id, status);
        return ResultVO.success(data);
    }

    /**
     * 我的好友列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < UserFriendPageVO>>
     */
    @Operation(summary = "我的好友列表")
    @GetMapping("page")
    public ResultVO<IPage<UserFriendPageVO>> page(PageDTO<UserFriendDO> page, @Validated UserFriendSearchDTO entity) {
        entity.setCurrentUserId(this.getUserInfo().getUserId());
        entity.setOrgId(this.getUserInfo().getOrgId());
        IPage<UserFriendPageVO> data = this.userFriendQueryService.page(page, entity);
        return ResultVO.success(data);
    }
}
