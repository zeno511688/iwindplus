/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.im.application.query.FriendChatMsgQueryService;
import com.iwindplus.im.application.query.dto.FriendChatMsgSearchDTO;
import com.iwindplus.im.common.enums.MsgStatusEnum;
import com.iwindplus.im.application.query.vo.FriendChatMsgPageVO;
import com.iwindplus.im.application.query.vo.FriendChatMsgVO;
import com.iwindplus.im.infrastructure.persistence.es.FriendChatMsgDO;
import com.iwindplus.im.application.service.FriendChatMsgApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 好友聊天消息相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "好友聊天消息接口")
@Slf4j
@RestController
@RequestMapping("admin/im/friend/chat/msg")
@Validated
@RequiredArgsConstructor
public class FriendChatMsgController extends BaseController {

    private final FriendChatMsgQueryService friendChatMsgQueryService;
    private final FriendChatMsgApplicationService friendChatMsgApplicationService;

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
        boolean data = this.friendChatMsgApplicationService.removeByIds(ids);
        return ResultVO.success(data);
    }

    /**
     * 编辑状态.
     *
     * @param id     主键
     * @param status 状态
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "编辑消息状态")
    @PutMapping("editMsgStatus")
    public ResultVO<Boolean> editMsgStatus(@RequestParam String id, @RequestParam MsgStatusEnum status) {
        boolean data = this.friendChatMsgApplicationService.editMsgStatus(id, status);
        return ResultVO.success(data);
    }

    /**
     * 列表）.
     *
     * @param entity 对象
     * @return ResultVO<IPage < FriendChatMsgPageVO>>
     */
    @Operation(summary = "列表")
    @GetMapping("page")
    public ResultVO<IPage<FriendChatMsgPageVO>> page(PageDTO<FriendChatMsgDO> page, @Validated FriendChatMsgSearchDTO entity) {
        entity.setCurrentUserId(this.getUserInfo().getUserId());
        entity.setOrgId(this.getUserInfo().getOrgId());
        IPage<FriendChatMsgPageVO> data = this.friendChatMsgQueryService.page(page, entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id         主键
     * @return ResultVO<FriendChatMsgVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<FriendChatMsgVO> getDetail(@RequestParam String id) {
        FriendChatMsgVO data = this.friendChatMsgQueryService.getDetail(id);
        return ResultVO.success(data);
    }
}
