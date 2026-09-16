/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.interfaces.api;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.log.api.SmsCaptchaLogApi;
import com.iwindplus.log.api.dto.SmsCaptchaLogDTO;
import com.iwindplus.log.api.dto.SmsSendValidDTO;
import com.iwindplus.log.application.service.SmsCaptchaLogApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短信验证码日志相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class SmsCaptchaLogApiImpl extends BaseController implements SmsCaptchaLogApi {

    private final SmsCaptchaLogApplicationService smsCaptchaLogApplicationService;

    @Override
    public ResultVO<String> save(SmsCaptchaLogDTO entity) {
        String data = this.smsCaptchaLogApplicationService.save(entity);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<Boolean> checkCanSend(SmsSendValidDTO entity) {
        boolean data = this.smsCaptchaLogApplicationService.checkCanSend(entity);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<Boolean> validate(String tplCode, String mobile, String captcha) {
        boolean data = this.smsCaptchaLogApplicationService.validate(tplCode, mobile, captcha);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<Boolean> validateByUserId(String tplCode, Long userId, Long orgId, String captcha) {
        boolean data = this.smsCaptchaLogApplicationService.validateByUserId(tplCode, userId, orgId, captcha);
        return ResultVO.success(data);
    }
}
