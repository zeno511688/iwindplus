/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.server.web.api;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.log.api.MailCaptchaLogApi;
import com.iwindplus.log.domain.dto.MailCaptchaLogDTO;
import com.iwindplus.log.domain.dto.MailSendValidDTO;
import com.iwindplus.log.server.service.MailCaptchaLogService;
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

    private final MailCaptchaLogService mailCaptchaLogService;

    @Override
    public ResultVO<String> save(MailCaptchaLogDTO entity) {
        String data = this.mailCaptchaLogService.save(entity);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<Boolean> checkCanSend(MailSendValidDTO entity) {
        boolean data = this.mailCaptchaLogService.checkCanSend(entity);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<Boolean> validate(String tplCode, String mail, String captcha) {
        boolean data = this.mailCaptchaLogService.validate(tplCode, mail, captcha);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<Boolean> validateByUserId(String tplCode, Long userId, Long orgId, String captcha) {
        boolean data = this.mailCaptchaLogService.validateByUserId(tplCode, userId, orgId, captcha);
        return ResultVO.success(data);
    }
}
