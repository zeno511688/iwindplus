/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.server.service;

import com.iwindplus.setup.domain.dto.SmsSendDTO;

/**
 * 短信业务层接口类.
 *
 * @author zengdegui
 * @since 2021/7/20
 */
public interface SmsService {

    /**
     * 发送手机验证码（手机）.
     *
     * @param requestId 请求唯一标识（可选）
     * @param tplCode   模板编码（必填）
     * @param mobile    手机（必填）
     */
    void sendCaptcha(String requestId, String tplCode, String mobile);

    /**
     * 发送手机验证码（用户主键）.
     *
     * @param requestId 请求唯一标识（可选）
     * @param tplCode   模板编码（必填）
     * @param userId    用户主键（必填）
     * @param orgId     组织主键（必填）
     */
    void sendCaptchaByUserId(String requestId, String tplCode, Long userId, Long orgId);

    /**
     * 发送短信.
     *
     * @param entity 对象
     * @return
     */
    void send(SmsSendDTO entity);

}
