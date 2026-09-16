/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 短信验证码发送参数.
 *
 * @author zengdegui
 * @since 2026/9/6
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SmsSendCaptchaDTO extends SmsSendBaseDTO {

    /**
     * 手机（必填）.
     */
    private String phoneNumber;

    /**
     * 模板内容（必填，业务模板内容，由调用方传入）.
     */
    private String templateContent;

    /**
     * 短信签名（可选，部分服务商需要，如阿里云）.
     */
    private String signName;

    /**
     * 短信验证码长度（默认：6）.
     */
    private Integer captchaLength;

    /**
     * 短信验证码有效时间（单位：分钟，默认：10）.
     */
    private Integer captchaTimeout;
}
