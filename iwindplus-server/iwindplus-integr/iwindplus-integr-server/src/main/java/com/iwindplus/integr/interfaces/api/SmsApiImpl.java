/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.interfaces.api;

import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.integr.api.SmsApi;
import com.iwindplus.integr.application.service.SmsApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短信相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class SmsApiImpl extends BaseController implements SmsApi {

    private final SmsApplicationService smsService;

    @Override
    public void sendCaptcha(String tplCode, String mobile) {
        this.smsService.sendCaptcha(tplCode, mobile);
    }

    @Override
    public void sendCaptchaByUserId(String tplCode, Long userId, Long orgId) {
        this.smsService.sendCaptchaByUserId(tplCode, userId, orgId);
    }
}
