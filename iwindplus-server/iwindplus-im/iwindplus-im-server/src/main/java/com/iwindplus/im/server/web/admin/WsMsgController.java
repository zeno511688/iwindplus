/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.server.web.admin;

import cn.hutool.core.bean.BeanUtil;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.im.domain.dto.WsMsgDTO;
import com.iwindplus.im.domain.dto.WsSendMsgDTO;
import com.iwindplus.im.domain.enums.CommandEnum;
import com.iwindplus.im.server.service.ws.WsMsgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ws消息推送相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2023/12/04 23:10
 */
@Tag(name = "消息推送接口")
@Slf4j
@RestController
@RequestMapping("admin/im/ws")
@Validated
public class WsMsgController extends BaseController {

    @Resource
    private WsMsgService wsMsgService;

    /**
     * 发送个人消息通知.
     *
     * @param entity 对象
     */
    @Operation(summary = "发送个人消息通知")
    @PostMapping("sendPersonNoticeMsg")
    public void sendPersonNoticeMsg(@RequestBody @Validated WsMsgDTO entity) {
        UserBaseVO userInfo = this.getUserInfo();
        final WsSendMsgDTO param = BeanUtil.copyProperties(entity, WsSendMsgDTO.class);
        param.setCommand(CommandEnum.PERSON_NOTICE_MSG);
        param.setSendUserId(userInfo.getUserId());
        param.setSendOrgId(userInfo.getOrgId());
        this.wsMsgService.sendWsMsg(param);
    }

    /**
     * 发送系统消息通知.
     *
     * @param entity 对象
     */
    @Operation(summary = "发送系统消息通知")
    @PostMapping("sendSystemNoticeMsg")
    public void sendSystemNoticeMsg(@RequestBody @Validated WsMsgDTO entity) {
        UserBaseVO userInfo = this.getUserInfo();
        final WsSendMsgDTO param = BeanUtil.copyProperties(entity, WsSendMsgDTO.class);
        param.setCommand(CommandEnum.SYS_NOTICE_MSG);
        param.setSendUserId(userInfo.getUserId());
        param.setSendOrgId(userInfo.getOrgId());
        this.wsMsgService.sendWsMsg(param);
    }

    /**
     * 发送消息通知.
     *
     * @param entity 对象
     */
    @Operation(summary = "发送消息通知")
    @PostMapping("sendWsMsg")
    public void sendWsMsg(@RequestBody @Validated WsSendMsgDTO entity) {
        UserBaseVO userInfo = this.getUserInfo();
        entity.setSendUserId(userInfo.getUserId());
        entity.setSendOrgId(userInfo.getOrgId());
        this.wsMsgService.sendWsMsg(entity);
    }
}
