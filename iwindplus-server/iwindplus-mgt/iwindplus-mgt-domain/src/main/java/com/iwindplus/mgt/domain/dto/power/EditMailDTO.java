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
 * 用户邮箱绑定（更改）数据传输对象.
 *
 * @author zengdegui
 * @since 2021/12/10
 */
@Schema(description = "用户邮箱绑定（更改）数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EditMailDTO implements Serializable {

    /**
     * 邮箱.
     */
    @Schema(description = "邮箱")
    @NotBlank(message = "{mail.notEmpty}")
    @Length(max = 100, message = "{mail.length}")
    private String mail;

    /**
     * 验证码.
     */
    @Schema(description = "验证码")
    @NotBlank(message = "{captcha.notEmpty}")
    private String captcha;

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    private Long userId;
}
