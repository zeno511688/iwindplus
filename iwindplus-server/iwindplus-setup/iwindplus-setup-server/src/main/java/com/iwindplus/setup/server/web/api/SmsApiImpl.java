/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.setup.server.web.api;

import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.setup.api.SmsApi;
import com.iwindplus.setup.server.service.SmsService;
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

    private final SmsService smsService;

    @Override
    public void sendCaptcha(String requestId, String tplCode, String mobile) {
        this.smsService.sendCaptcha(requestId, tplCode, mobile);
    }

    @Override
    public void sendCaptchaByUserId(String requestId, String tplCode, Long userId, Long orgId) {
        this.smsService.sendCaptchaByUserId(requestId, tplCode, userId, orgId);
    }
}
