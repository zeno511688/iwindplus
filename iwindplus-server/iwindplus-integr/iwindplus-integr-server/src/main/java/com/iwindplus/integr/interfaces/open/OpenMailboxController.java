/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.interfaces.open;

import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.integr.application.service.MailboxApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邮箱对外相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2021/7/20
 */
@Tag(name = "邮箱对外接口")
@Slf4j
@RestController
@RequestMapping("openapi/integr/mailbox")
@Validated
@RequiredArgsConstructor
public class OpenMailboxController extends BaseController {

    private final MailboxApplicationService mailboxService;

    public static final String MAIL_TPL_CODE = "c3f67fd355dd6098156053f68285ba3e";

    /**
     * 发送邮箱验证码（邮箱）.
     *
     * @param mail    邮箱（必填）
     */
    @Operation(summary = "发送邮箱验证码（邮箱）")
    @PostMapping("sendCaptcha")
    public void sendCaptcha(
        @RequestParam String mail) {
        final String tplCode = MAIL_TPL_CODE;
        this.mailboxService.sendCaptcha(tplCode, mail);
    }
}
