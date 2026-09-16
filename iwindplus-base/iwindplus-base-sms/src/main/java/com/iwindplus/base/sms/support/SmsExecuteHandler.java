/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.support;

import com.iwindplus.base.domain.enums.SmsTypeEnum;
import com.iwindplus.base.sms.domain.dto.SmsSendCaptchaDTO;
import com.iwindplus.base.sms.domain.dto.SmsSendRequestDTO;
import com.iwindplus.base.sms.domain.vo.SmsSendBatchResultVO;
import com.iwindplus.base.sms.domain.vo.SmsSendResultVO;
import com.iwindplus.base.sms.service.BaseService;
import java.util.List;
import java.util.Optional;

/**
 * 短信策略标识接口.
 *
 * @author zengdegui
 * @since 2024/9/1
 */
public interface SmsExecuteHandler extends BaseService {

    /**
     * 获取提供商类型.
     *
     * @return 提供商枚举
     */
    SmsTypeEnum getProvider();

    /**
     * 发送短信验证码.
     *
     * @param entity 短信验证码发送参数
     * @return 短信日志，发送失败时返回 {@link Optional#empty()}
     */
    Optional<SmsSendResultVO> smsSendCaptcha(SmsSendCaptchaDTO entity);

    /**
     * 发送短信，成功返回流水号.
     *
     * @param entity 短信发送参数
     * @return 批量发送结果，发送失败时返回 {@link Optional#empty()}
     */
    Optional<List<SmsSendBatchResultVO>> smsSend(SmsSendRequestDTO entity);
}
