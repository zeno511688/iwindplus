/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.server.web.api;

import com.iwindplus.base.util.BeanCopierUtil;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.im.api.WsMsgApi;
import com.iwindplus.im.domain.dto.WsMsgDTO;
import com.iwindplus.im.domain.dto.WsSendMsgDTO;
import com.iwindplus.im.domain.enums.CommandEnum;
import com.iwindplus.im.server.service.ws.WsMsgService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ws消息推送相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class WsMsgApiImpl extends BaseController implements WsMsgApi {

    private final WsMsgService wsMsgService;

    @Override
    public void sendPersonNoticeMsg(WsMsgDTO entity) {
        final WsSendMsgDTO param = BeanCopierUtil.copyProperties(entity, WsSendMsgDTO::new);
        param.setCommand(CommandEnum.PERSON_NOTICE_MSG);
        this.wsMsgService.sendWsMsg(param);
    }

    @Override
    public void sendSystemNoticeMsg(WsMsgDTO entity) {
        final WsSendMsgDTO param = BeanCopierUtil.copyProperties(entity, WsSendMsgDTO::new);
        param.setCommand(CommandEnum.SYS_NOTICE_MSG);
        this.wsMsgService.sendWsMsg(param);
    }
}
