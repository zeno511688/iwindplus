/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.interfaces.api;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.log.api.MailCaptchaLogApi;
import com.iwindplus.log.api.dto.MailCaptchaLogDTO;
import com.iwindplus.log.api.dto.MailSendValidDTO;
import com.iwindplus.log.application.service.MailCaptchaLogApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 邮箱验证码日志相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class MailCaptchaLogApiImpl extends BaseController implements MailCaptchaLogApi {

    private final MailCaptchaLogApplicationService mailCaptchaLogApplicationService;

    @Override
    public ResultVO<String> save(MailCaptchaLogDTO entity) {
        String data = this.mailCaptchaLogApplicationService.save(entity);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<Boolean> checkCanSend(MailSendValidDTO entity) {
        boolean data = this.mailCaptchaLogApplicationService.checkCanSend(entity);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<Boolean> validate(String tplCode, String mail, String captcha) {
        boolean data = this.mailCaptchaLogApplicationService.validate(tplCode, mail, captcha);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<Boolean> validateByUserId(String tplCode, Long userId, Long orgId, String captcha) {
        boolean data = this.mailCaptchaLogApplicationService.validateByUserId(tplCode, userId, orgId, captcha);
        return ResultVO.success(data);
    }
}
