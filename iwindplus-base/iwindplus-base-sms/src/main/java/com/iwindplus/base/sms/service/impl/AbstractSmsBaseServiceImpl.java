/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.sms.domain.constant.SmsConstant;
import com.iwindplus.base.sms.domain.vo.SmsBatchVO;
import com.iwindplus.base.sms.domain.vo.SmsLogVO;
import com.iwindplus.base.sms.service.SmsBaseService;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 短信业务基础抽象类.
 *
 * @author zengdegui
 * @since 2020/3/13
 */
@Slf4j
public abstract class AbstractSmsBaseServiceImpl extends AbstractSmsBaseConfigServiceImpl implements SmsBaseService {

    /**
     * 获取短信日志.
     *
     * @param captchaTimeout 验证码有效期
     * @param result         批量发送结果
     * @return SmsLogVO
     */
    protected SmsLogVO getSmsLogVO(Integer captchaTimeout, List<SmsBatchVO> result) {
        SmsLogVO data = null;
        if (CollUtil.isNotEmpty(result)) {
            SmsBatchVO batchVO = result.get(0);
            LocalDateTime expireTime = LocalDateTime.now().plusMinutes(Optional.ofNullable(captchaTimeout).orElse(SmsConstant.CAPTCHA_TIMEOUT));
            data = SmsLogVO.builder()
                .bizNumber(batchVO.getBizNumber())
                .phoneNumber(CollUtil.isNotEmpty(batchVO.getPhoneNumbers()) ? batchVO.getPhoneNumbers().get(0) : null)
                .captcha(CollUtil.isNotEmpty(batchVO.getTemplateParams()) ? batchVO.getTemplateParams().get(0) : null)
                .expireTime(expireTime)
                .build();
        }
        return data;
    }
}
