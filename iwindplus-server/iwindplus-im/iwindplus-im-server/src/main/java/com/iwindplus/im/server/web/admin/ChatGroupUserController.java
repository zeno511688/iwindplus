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
import com.iwindplus.im.domain.dto.ChatGroupUserSearchDTO;
import com.iwindplus.im.domain.vo.ChatGroupUserPageVO;
import com.iwindplus.im.server.dal.model.ChatGroupUserDO;
import com.iwindplus.im.server.service.ChatGroupUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 聊天群用户相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "聊天群用户接口")
@Slf4j
@RestController
@RequestMapping("admin/im/chat/group/user")
@Validated
public class ChatGroupUserController extends BaseController {

    @Resource
    private ChatGroupUserService chatGroupUserService;

    /**
     * 聊天群用户列表.
     *
     * @param entity 对象
     * @return ResultVO<IPage < ChatGroupUserPageVO>>
     */
    @Operation(summary = "聊天群用户列表")
    @GetMapping("page")
    public ResultVO<IPage<ChatGroupUserPageVO>> page(PageDTO<ChatGroupUserDO> page, @Validated ChatGroupUserSearchDTO entity) {
        entity.setCurrentUserId(this.getUserInfo().getUserId());
        entity.setOrgId(this.getUserInfo().getOrgId());
        IPage<ChatGroupUserPageVO> data = this.chatGroupUserService.page(page, entity);
        return ResultVO.success(data);
    }
}
