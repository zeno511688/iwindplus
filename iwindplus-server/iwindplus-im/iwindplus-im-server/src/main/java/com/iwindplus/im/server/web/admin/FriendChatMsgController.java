/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.server.web.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.operate.domain.annotation.OperateValid;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.im.domain.dto.FriendChatMsgSearchDTO;
import com.iwindplus.im.domain.enums.MsgStatusEnum;
import com.iwindplus.im.domain.vo.FriendChatMsgPageVO;
import com.iwindplus.im.domain.vo.FriendChatMsgVO;
import com.iwindplus.im.server.dal.model.FriendChatMsgDO;
import com.iwindplus.im.server.service.FriendChatMsgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
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
public class FriendChatMsgController extends BaseController {

    @Resource
    private FriendChatMsgService friendChatMsgService;

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
        boolean data = this.friendChatMsgService.removeByIds(ids);
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
        boolean data = this.friendChatMsgService.editMsgStatus(id, status);
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
        IPage<FriendChatMsgPageVO> data = this.friendChatMsgService.page(page, entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id         主键
     * @param ossTplCode 对象存储模板配置编码
     * @return ResultVO<FriendChatMsgVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<FriendChatMsgVO> getDetail(@RequestParam String id, @RequestParam(required = false) String ossTplCode) {
        FriendChatMsgVO data = this.friendChatMsgService.getDetail(id, ossTplCode);
        return ResultVO.success(data);
    }
}
