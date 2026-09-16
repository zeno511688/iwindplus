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
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.im.application.query.GroupChatMsgQueryService;
import com.iwindplus.im.application.query.dto.GroupChatMsgSearchDTO;
import com.iwindplus.im.application.query.vo.GroupChatMsgPageVO;
import com.iwindplus.im.application.query.vo.GroupChatMsgVO;
import com.iwindplus.im.infrastructure.persistence.es.GroupChatMsgDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class GroupChatMsgController extends BaseController {

    private final GroupChatMsgQueryService groupChatMsgQueryService;

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
        IPage<GroupChatMsgPageVO> data = this.groupChatMsgQueryService.page(page, entity);
        return ResultVO.success(data);
    }

    /**
     * 详情.
     *
     * @param id         主键
     * @return ResultVO<GroupChatMsgVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getDetail")
    public ResultVO<GroupChatMsgVO> getDetail(@RequestParam String id) {
        GroupChatMsgVO data = this.groupChatMsgQueryService.getDetail(id);
        return ResultVO.success(data);
    }
}
