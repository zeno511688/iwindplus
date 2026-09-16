/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.interfaces.api;

import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.integr.api.MailboxApi;
import com.iwindplus.integr.api.dto.MailboxSendDTO;
import com.iwindplus.integr.application.service.MailboxApplicationService;
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

    private final MailboxApplicationService mailboxService;

    @Override
    public void send(MailboxSendDTO entity) {
        this.mailboxService.send(entity);
    }

    @Override
    public void sendCaptcha(String tplCode, String mail) {
        this.mailboxService.sendCaptcha(tplCode, mail);
    }

    @Override
    public void sendCaptchaByUserId(String tplCode, Long userId, Long orgId) {
        this.mailboxService.sendCaptchaByUserId(tplCode, userId, orgId);
    }
}
