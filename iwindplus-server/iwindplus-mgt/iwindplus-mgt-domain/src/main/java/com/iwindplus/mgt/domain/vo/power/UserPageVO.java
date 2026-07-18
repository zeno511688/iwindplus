/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.power;

import com.iwindplus.base.domain.annotation.Sensitive;
import com.iwindplus.base.domain.enums.SensitiveTypeEnum;
import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户分页视图对象.
 *
 * @author zengdegui
 * @since 2021/9/23
 */
@Schema(description = "用户分页视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserPageVO extends DbVersionBaseVO {

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
     * 邮箱.
     */
    @Schema(description = "邮箱")
    @Sensitive(type = SensitiveTypeEnum.EMAIL)
    private String mail;

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
