/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.domain.dto.system;

import com.iwindplus.base.domain.enums.UserSexEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 第三方授权绑定用户数据传输对象.
 *
 * @author zengdegui
 * @since 2019/7/23
 */
@Schema(description = "第三方授权绑定用户数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdBindGrantUserDTO implements Serializable {

    /**
     * 编码.
     */
    @Schema(description = "编码")
    @NotBlank(message = "{code.notEmpty}")
    private String code;

    /**
     * 手机.
     */
    @Schema(description = "手机")
    @NotBlank(message = "{mobile.notEmpty}")
    private String mobile;

    /**
     * 验证码.
     */
    @Schema(description = "验证码")
    @NotBlank(message = "{captcha.notEmpty}")
    @Length(max = 50, message = "{captcha.length}")
    private String captcha;

    /**
     * 昵称.
     */
    @Schema(description = "昵称")
    private String nickName;

    /**
     * 性别.
     */
    @Schema(description = "性别")
    private UserSexEnum sex;

    /**
     * 国家.
     */
    @Schema(description = "国家")
    private String country;

    /**
     * 省份.
     */
    @Schema(description = "省份")
    private String province;

    /**
     * 城市.
     */
    @Schema(description = "城市")
    private String city;

    /**
     * 头像.
     */
    @Schema(description = "头像")
    private String avatar;
}
