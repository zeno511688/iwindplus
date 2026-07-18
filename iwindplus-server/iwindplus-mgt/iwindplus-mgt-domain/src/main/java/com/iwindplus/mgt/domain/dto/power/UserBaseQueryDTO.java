/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.power;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 用户基础查询数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "用户基础查询数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserBaseQueryDTO implements Serializable {

    /**
     * 工号
     */
    @Schema(description = "工号")
    @Length(max = 50, message = "{jobNumber.length}")
    private String jobNumber;

    /**
     * 用户名.
     */
    @Schema(description = "用户名")
    @Length(max = 50, message = "{username.length}")
    private String username;

    /**
     * 手机.
     */
    @Schema(description = "手机")
    @Length(max = 50, message = "{mobile.length}")
    private String mobile;

    /**
     * 邮箱.
     */
    @Schema(description = "邮箱")
    @Length(max = 100, message = "{mail.length}")
    private String mail;

    /**
     * 身份证.
     */
    @Schema(description = "身份证")
    @Length(max = 50, message = "{idCard.length}")
    private String idCard;

}