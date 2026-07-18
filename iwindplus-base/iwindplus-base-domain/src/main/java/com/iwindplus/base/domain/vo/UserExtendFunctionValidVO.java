/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户扩展功能验证视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "用户扩展功能验证视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserExtendFunctionValidVO implements Serializable {

    /**
     * 是否绑定GA.
     */
    @Schema(description = "是否绑定GA")
    private Boolean gaBindFlag;

    /**
     * GA验证码验证结果.
     */
    @Schema(description = "GA验证码验证结果")
    private Boolean gaCheckFlag;

    /**
     * 是否绑定邮箱.
     */
    @Schema(description = "是否绑定邮箱")
    private Boolean mailBindFlag;

    /**
     * 邮箱验证码验证结果.
     */
    @Schema(description = "邮箱验证码验证结果")
    private Boolean mailCheckFlag;

    /**
     * 是否绑定手机.
     */
    @Schema(description = "是否绑定手机")
    private Boolean mobileBindFlag;

    /**
     * 短信验证码验证结果.
     */
    @Schema(description = "短信验证码验证结果")
    private Boolean smsCheckFlag;

    /**
     * 是否绑定yubikey.
     */
    @Schema(description = "是否绑定yubikey")
    private Boolean yubikeyBindFlag;

    /**
     * yubikey证结果.
     */
    @Schema(description = "yubikey验证结果")
    private Boolean yubikeyCheckFlag;
}
