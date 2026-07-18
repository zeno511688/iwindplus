/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.domain.vo.power;

import com.iwindplus.base.domain.annotation.Sensitive;
import com.iwindplus.base.domain.enums.SensitiveTypeEnum;
import com.iwindplus.base.domain.enums.UserSexEnum;
import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户详情视图对象.
 *
 * @author zengdegui
 * @since 2021/9/23
 */
@Schema(description = "用户详情视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO extends DbVersionBaseVO {

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
    @Sensitive(type = SensitiveTypeEnum.CUSTOM, startInclude = 2, endReserve = 2)
    private String username;

    /**
     * 手机.
     */
    @Schema(description = "手机")
    @Sensitive(type = SensitiveTypeEnum.MOBILE_PHONE)
    private String mobile;

    /**
     * 姓名.
     */
    @Schema(description = "姓名")
    @Sensitive(type = SensitiveTypeEnum.FIRST_MASK)
    private String realName;

    /**
     * 性别（UNKNOWN：未知，MALE：男，FEMALE：女 ）.
     */
    @Schema(description = "性别（UNKNOWN：未知，MALE：男，FEMALE：女 ）")
    private UserSexEnum sex;

    /**
     * 邮箱.
     */
    @Schema(description = "邮箱")
    @Sensitive(type = SensitiveTypeEnum.EMAIL)
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
    @Sensitive(type = SensitiveTypeEnum.ID_CARD)
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
     * 年龄.
     */
    @Schema(description = "年龄")
    private Integer age;

    /**
     * 是否管理员.
     */
    @Schema(description = "是否管理员")
    private Boolean adminFlag;

    /**
     * GA密钥.
     */
    @Schema(description = "GA密钥")
    private String gaSecret;

    /**
     * 是否绑定GA.
     */
    @Schema(description = "是否绑定GA")
    private Boolean gaBindFlag;
}
