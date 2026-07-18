/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.setup.server.web.api;

import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.setup.api.MailboxApi;
import com.iwindplus.setup.domain.dto.MailboxSendDTO;
import com.iwindplus.setup.server.service.MailboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邮箱相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class MailboxApiImpl extends BaseController implements MailboxApi {

    private final MailboxService mailboxService;

    @Override
    public void send(MailboxSendDTO entity) {
        this.mailboxService.send(entity);
    }

    @Override
    public void sendCaptcha(String requestId, String tplCode, String mail) {
        this.mailboxService.sendCaptcha(requestId, tplCode, mail);
    }

    @Override
    public void sendCaptchaByUserId(String requestId, String tplCode, Long userId, Long orgId) {
        this.mailboxService.sendCaptchaByUserId(requestId, tplCode, userId, orgId);
    }
}
