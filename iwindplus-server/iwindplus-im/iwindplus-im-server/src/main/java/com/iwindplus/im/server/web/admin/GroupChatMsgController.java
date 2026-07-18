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
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.im.domain.dto.GroupChatMsgSearchDTO;
import com.iwindplus.im.domain.vo.GroupChatMsgPageVO;
import com.iwindplus.im.domain.vo.GroupChatMsgVO;
import com.iwindplus.im.server.dal.model.GroupChatMsgDO;
import com.iwindplus.im.server.service.GroupChatMsgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 群聊消息相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "群聊消息接口")
@Slf4j
@RestController
@RequestMapping("admin/im/group/chat/msg")
@Validated
public class GroupChatMsgController extends BaseController {

    @Resource
    private GroupChatMsgService groupChatMsgService;

    /**
     * 群聊消息列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < GroupChatMsgPageVO>>
     */
    @Operation(summary = "群聊消息列表")
    @GetMapping("page")
    public ResultVO<IPage<GroupChatMsgPageVO>> page(PageDTO<GroupChatMsgDO> page, @Validated GroupChatMsgSearchDTO entity) {
        entity.setCurrentUserId(this.getUserInfo().getUserId());
        entity.setOrgId(this.getUserInfo().getOrgId());
        IPage<GroupChatMsgPageVO> data = this.groupChatMsgService.page(page, entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id         主键
     * @param ossTplCode 对象存储模板配置编码
     * @return ResultVO<GroupChatMsgVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<GroupChatMsgVO> getDetail(@RequestParam String id, @RequestParam(required = false) String ossTplCode) {
        GroupChatMsgVO data = this.groupChatMsgService.getDetail(id, ossTplCode);
        return ResultVO.success(data);
    }
}
