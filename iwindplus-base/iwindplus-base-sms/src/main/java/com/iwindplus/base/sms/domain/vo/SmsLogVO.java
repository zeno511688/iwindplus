/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短信日志视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "短信日志视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SmsLogVO implements Serializable {

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

    /**
     * 手机.
     */
    @Schema(description = "手机")
    private String phoneNumber;

    /**
     * 验证码.
     */
    @Schema(description = "验证码")
    private String captcha;

    /**
     * 过期时间.
     */
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

}