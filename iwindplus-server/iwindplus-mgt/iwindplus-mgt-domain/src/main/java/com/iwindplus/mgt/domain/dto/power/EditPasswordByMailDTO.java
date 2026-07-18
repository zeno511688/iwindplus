/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.domain.dto.power;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * 忘记密码（通过邮箱找回）数据传输对象.
 *
 * @author zengdegui
 * @since 2019/7/23
 */
@Schema(description = "忘记密码（通过邮箱找回）数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EditPasswordByMailDTO implements Serializable {

    /**
     * 邮箱（必填）.
     */
    @Schema(description = "邮箱")
    @NotBlank(message = "{mail.notEmpty}")
    private String mail;

    /**
     * 验证码（必填）.
     */
    @Schema(description = "验证码")
    @NotBlank(message = "{captcha.notEmpty}")
    private String captcha;

    /**
     * 新密码（必填）.
     */
    @Schema(description = "新密码")
    @NotBlank(message = "{newPassword.notEmpty}")
    @Length(max = 50, message = "{newPassword.length}")
    private String newPassword;
}
