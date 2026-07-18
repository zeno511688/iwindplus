/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.power;

import com.iwindplus.base.domain.annotation.Sensitive;
import com.iwindplus.base.domain.enums.SensitiveTypeEnum;
import com.iwindplus.base.domain.enums.UserSexEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户信息视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "用户信息视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO implements Serializable {

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    private Long userId;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;

    /**
     * 组织编码.
     */
    @Schema(description = "组织编码")
    private String orgCode;

    /**
     * 组织名称.
     */
    @Schema(description = "组织名称")
    private String orgName;

    /**
     * 组织简称.
     */
    @Schema(description = "组织简称")
    private String orgAbbr;

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
     * 身份证.
     */
    @Schema(description = "身份证")
    @Sensitive(type = SensitiveTypeEnum.ID_CARD)
    private String idCard;

    /**
     * 头像（相对路径）.
     */
    @Schema(description = "头像（相对路径）")
    private String avatar;

    /**
     * 是否管理员.
     */
    @Schema(description = "是否管理员")
    private Boolean adminFlag;

    /**
     * 是否绑定GA.
     */
    @Schema(description = "是否绑定GA")
    private Boolean gaBindFlag;

}
