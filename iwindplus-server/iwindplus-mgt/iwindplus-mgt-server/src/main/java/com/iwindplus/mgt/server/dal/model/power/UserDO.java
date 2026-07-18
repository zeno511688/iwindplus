/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.model.power;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwindplus.base.mybatis.domain.DbSignBaseDO;
import com.iwindplus.base.domain.annotation.TableFieldSafe;
import com.iwindplus.base.domain.enums.UserSexEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户表.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "用户对象")
@TableName(value = "`user`")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserDO extends DbSignBaseDO {

    /**
     * 账号是否启用.
     */
    @Schema(description = "账号是否启用")
    private Boolean enabled;

    /**
     * 账号是否锁定.
     */
    @Email
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
    private String username;

    /**
     * 手机.
     */
    @Schema(description = "手机")
    private String mobile;

    /**
     * 姓名.
     */
    @Schema(description = "姓名")
    private String realName;

    /**
     * 密码（md5加密）.
     */
    @Schema(description = "密码（md5加密）")
    @JsonIgnore
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
    private String mail;

    /**
     * 昵称.
     */
    @Schema(description = "昵称")
    private String nickName;

    /**
     * 头像（相对路径）.
     */
    @Schema(description = "头像（相对路径）")
    private String avatar;

    /**
     * 生日.
     */
    @Schema(description = "生日")
    private String birthday;

    /**
     * 身份证.
     */
    @Schema(description = "身份证")
    private String idCard;

    /**
     * 身份证正面.
     */
    @Schema(description = "身份证正面")
    private String idCardFront;

    /**
     * 身份证背面.
     */
    @Schema(description = "身份证背面")
    private String idCardBack;

    /**
     * 国家（身份证）.
     */
    @Schema(description = "国家（身份证）")
    private String country;

    /**
     * 省份（身份证）.
     */
    @Schema(description = "省份（身份证）")
    private String province;

    /**
     * 城市（身份证）.
     */
    @Schema(description = "城市（身份证）")
    private String city;

    /**
     * 地区（身份证）.
     */
    @Schema(description = "地区（身份证）")
    private String district;

    /**
     * 街道/详细地址（身份证）.
     */
    @Schema(description = "街道/详细地址（身份证）")
    private String detailAddress;

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

    /**
     * GA密钥.
     */
    @TableFieldSafe
    @Schema(description = "GA密钥")
    private String gaSecret;

    /**
     * 是否绑定GA.
     */
    @Schema(description = "是否绑定GA")
    private Boolean gaBindFlag;
}