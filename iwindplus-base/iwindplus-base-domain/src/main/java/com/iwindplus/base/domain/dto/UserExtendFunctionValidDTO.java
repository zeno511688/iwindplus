/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户扩展功能验证数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "用户扩展功能验证数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserExtendFunctionValidDTO implements Serializable {

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    @NotNull(message = "{userId.notEmpty}")
    private Long userId;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    @NotNull(message = "{orgId.notEmpty}")
    private Long orgId;

    /**
     * GA验证码.
     */
    @Schema(description = "GA验证码")
    private String gaCaptcha;

    /**
     * 邮箱验证码.
     */
    @Schema(description = "邮箱验证码")
    private String mailCaptcha;

    /**
     * 短信验证码.
     */
    @Schema(description = "短信验证码")
    private String smsCaptcha;

    /**
     * yubikey原数据.
     */
    @Schema(description = "yubikey原数据")
    private String yubikeySource;

    /**
     * yubikey签名数据.
     */
    @Schema(description = "yubikey签名数据")
    private String yubikeySign;
}