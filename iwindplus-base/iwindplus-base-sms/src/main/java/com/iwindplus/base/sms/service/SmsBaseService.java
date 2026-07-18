/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.service;

import com.iwindplus.base.sms.domain.vo.SmsBatchVO;
import com.iwindplus.base.sms.domain.vo.SmsLogVO;

import java.util.List;

/**
 * 短信业务层基础接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface SmsBaseService extends SmsBaseConfigService {

    /**
     * 发送短信验证码.
     *
     * @param phoneNumber    手机（必填）
     * @param captchaLength  短信验证码长度（默认：6）
     * @param captchaTimeout 短信验证码有效时间（单位：分钟，默认：10）
     * @return SmsLogVO
     */
    SmsLogVO smsSendCaptcha(String phoneNumber, Integer captchaLength, Integer captchaTimeout);

    /**
     * 发送短信，成功返回流水号.
     *
     * @param phoneNumbers         手机号集合（必填）
     * @param templateParams       模板参数，用于替换短信模板中的参数（可选）
     * @param phoneNumberGroupSize 每个分组的手机个数（可选，默认：100）
     * @return List<SmsLogBaseVO>
     */
    List<SmsBatchVO> smsSend(List<String> phoneNumbers, List<String> templateParams, Integer phoneNumberGroupSize);
}
