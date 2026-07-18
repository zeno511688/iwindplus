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
 * 编辑密码数据传输对象.
 *
 * @author zengdegui
 * @since 2019/7/23
 */
@Schema(description = "编辑密码数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EditPasswordDTO implements Serializable {
    /**
     * 原密码（必填）.
     */
    @Schema(description = "原密码")
    @NotBlank(message = "{oldPassword.notEmpty}")
    private String oldPassword;

    /**
     * 新密码（必填）.
     */
    @Schema(description = "新密码")
    @NotBlank(message = "{newPassword.notEmpty}")
    @Length(max = 50, message = "{password.length}")
    private String newPassword;

    /**
     * 当前登录用户主键.
     */
    @Schema(description = "当前登录用户主键")
    private Long userId;
}
