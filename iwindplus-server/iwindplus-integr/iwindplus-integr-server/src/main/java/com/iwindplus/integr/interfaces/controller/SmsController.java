/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.interfaces.controller;

import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.integr.application.service.dto.SmsSendDTO;
import com.iwindplus.integr.application.service.SmsApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短信相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2021/7/20
 */
@Tag(name = "短信接口")
@Slf4j
@RestController
@RequestMapping("admin/integr/sms")
@Validated
@RequiredArgsConstructor
public class SmsController extends BaseController {

    private final SmsApplicationService smsApplicationService;

    public static final String SMS_TPL_CODE = "c3f67fd354dd6098154053f68285ba35";

    /**
     * 发送手机验证码（用户主键）.
     */
    @Operation(summary = "发送手机验证码（用户主键）")
    @PostMapping("sendCaptchaByUserId")
    public void sendCaptchaByUserId() {
        final Long userId = this.getUserInfo().getUserId();
        final Long orgId = this.getUserInfo().getOrgId();
        final String tplCode = SMS_TPL_CODE;
        this.smsApplicationService.sendCaptchaByUserId(tplCode, userId, orgId);
    }

    /**
     * 发送手机验证码（手机）.
     *
     * @param mobile 手机（必填）
     */
    @Operation(summary = "发送手机验证码（手机）")
    @PostMapping("sendCaptcha")
    public void sendCaptcha(@RequestParam String mobile) {
        final String tplCode = SMS_TPL_CODE;
        this.smsApplicationService.sendCaptcha(tplCode, mobile);
    }

    /**
     * 发送短信.
     *
     * @param entity 对象
     */
    @Operation(summary = "发送短信")
    @PostMapping("send")
    public void send(@RequestBody @Validated SmsSendDTO entity) {
        final String tplCode = SMS_TPL_CODE;
        entity.setTplCode(tplCode);
        this.smsApplicationService.send(entity);
    }

}
