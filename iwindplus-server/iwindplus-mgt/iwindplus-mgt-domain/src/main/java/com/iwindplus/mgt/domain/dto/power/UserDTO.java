/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.power;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.enums.UserSexEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.mgt.domain.validation.RegisterGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 用户数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "用户数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO extends DbVersionBaseDTO {

    /**
     * 账号是否启用.
     */
    @Schema(description = "账号是否启用")
    private Boolean enabled;

    /**
     * 账号是否锁定.
     */
    @Schema(description = "账号是否锁定")
    private Boolean locked;

    /**
     * 账号是否过期.
     */
    @Schema(description = "账号是否过期")
    private Boolean accountExpired;

    /**
     * 凭证（密码）是否过期.
     */
    @Schema(description = "凭证（密码）是否过期")
    private Boolean credentialsExpired;

    /**
     * 工号
     */
    @Schema(description = "工号")
    private String jobNumber;

    /**
     * 用户名.
     */
    @Schema(description = "用户名")
    @NotBlank(message = "{username.notEmpty}", groups = {SaveGroup.class, RegisterGroup.class})
    @Length(max = 50, message = "{username.length}", groups = {SaveGroup.class, EditGroup.class})
    private String username;

    /**
     * 手机.
     */
    @Schema(description = "手机")
    @NotBlank(message = "{mobile.notEmpty}", groups = {SaveGroup.class, RegisterGroup.class})
    @Length(max = 50, message = "{mobile.length}", groups = {SaveGroup.class, EditGroup.class})
    private String mobile;

    /**
     * 姓名.
     */
    @Schema(description = "姓名")
    @Length(max = 50, message = "{realName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String realName;

    /**
     * 密码（md5加密）.
     */
    @Schema(description = "密码（md5加密）")
    @JsonIgnore
    @Length(max = 50, message = "{password.length}", groups = {SaveGroup.class, EditGroup.class})
    private String password;

    /**
     * 性别（UNKNOWN：未知，MALE：男，FEMALE：女 ）.
     */
    @Schema(description = "性别（UNKNOWN：未知，MALE：男，FEMALE：女 ）")
    private UserSexEnum sex;

    /**
     * 邮箱.
     */
    @Schema(description = "邮箱")
    @Length(max = 100, message = "{mail.length}", groups = {SaveGroup.class, EditGroup.class})
    private String mail;

    /**
     * 昵称.
     */
    @Schema(description = "昵称")
    @Length(max = 50, message = "{nickName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String nickName;

    /**
     * 头像（相对路径）.
     */
    @Schema(description = "头像（相对路径）")
    @Length(max = 255, message = "{avatar.length}", groups = {SaveGroup.class, EditGroup.class})
    private String avatar;

    /**
     * 生日.
     */
    @Schema(description = "生日")
    @Length(max = 50, message = "{birthday.length}", groups = {SaveGroup.class, EditGroup.class})
    private String birthday;

    /**
     * 身份证.
     */
    @Schema(description = "身份证")
    @Length(max = 50, message = "{idCard.length}", groups = {SaveGroup.class, EditGroup.class})
    private String idCard;

    /**
     * 身份证正面.
     */
    @Schema(description = "身份证正面")
    @Length(max = 255, message = "{idCardFront.length}", groups = {SaveGroup.class, EditGroup.class})
    private String idCardFront;

    /**
     * 身份证背面.
     */
    @Schema(description = "身份证背面")
    @Length(max = 255, message = "{idCardBack.length}", groups = {SaveGroup.class, EditGroup.class})
    private String idCardBack;

    /**
     * 国家（身份证）.
     */
    @Schema(description = "国家（身份证）")
    @Length(max = 50, message = "{country.length}", groups = {SaveGroup.class, EditGroup.class})
    private String country;

    /**
     * 省份（身份证）.
     */
    @Schema(description = "省份（身份证）")
    @Length(max = 100, message = "{province.length}", groups = {SaveGroup.class, EditGroup.class})
    private String province;

    /**
     * 城市（身份证）.
     */
    @Schema(description = "城市（身份证）")
    @Length(max = 50, message = "{city.length}", groups = {SaveGroup.class, EditGroup.class})
    private String city;

    /**
     * 地区（身份证）.
     */
    @Schema(description = "地区（身份证）")
    @Length(max = 50, message = "{district.length}", groups = {SaveGroup.class, EditGroup.class})
    private String district;

    /**
     * 是否内置（false：否，true：是）.
     */
    @Schema(description = "是否内置（false：否，true：是）")
    private Boolean buildInFlag;

    /**
     * 是否管理员.
     */
    @Schema(description = "是否管理员")
    private Boolean adminFlag;
}