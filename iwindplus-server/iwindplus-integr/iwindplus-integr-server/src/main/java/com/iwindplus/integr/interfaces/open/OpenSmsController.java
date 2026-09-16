/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.interfaces.open;

import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.integr.application.service.SmsApplicationService;
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
 * 短信对外相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2021/7/20
 */
@Tag(name = "短信对外接口")
@Slf4j
@RestController
@RequestMapping("openapi/integr/sms")
@Validated
@RequiredArgsConstructor
public class OpenSmsController extends BaseController {

    private final SmsApplicationService smsService;

    public static final String SMS_TPL_CODE = "c3f67fd354dd6098154053f68285ba35";

    /**
     * 发送手机验证码（手机）.
     *
     * @param mobile  手机（必填）
     */
    @Operation(summary = "发送手机验证码（手机）")
    @PostMapping("sendCaptcha")
    public void sendCaptcha(
        @RequestParam String mobile) {
        final String tplCode = SMS_TPL_CODE;
        this.smsService.sendCaptcha(tplCode, mobile);
    }
}
