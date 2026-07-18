/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.system;

import com.iwindplus.base.domain.annotation.EnumValid;
import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.mgt.domain.enums.BindTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 第三方绑定授权数据传输对象.
 *
 * @author zengdegui
 * @since 2019/7/16
 */
@Schema(description = "第三方绑定授权数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdBindGrantDTO extends DbVersionBaseDTO {

    /**
     * 编码.
     */
    @Schema(description = "编码")
    @Length(max = 50, message = "{code.length}", groups = {SaveGroup.class, EditGroup.class})
    private String code;

    /**
     * 用户唯一标识.
     */
    @Schema(description = "用户唯一标识")
    @NotBlank(message = "{openid.notEmpty}", groups = {SaveGroup.class})
    @Length(max = 100, message = "{openid.length}", groups = {SaveGroup.class})
    private String openid;

    /**
     * 用户在开放平台的唯一标识符.
     */
    @Schema(description = "用户在开放平台的唯一标识符")
    @Length(max = 100, message = "{unionId.length}", groups = {SaveGroup.class})
    private String unionId;

    /**
     * 类型（MP：微信公众号，MA：微信小程序，CP：企业微信）.
     */
    @Schema(description = "类型（MP：微信公众号，MA：微信小程序，CP：企业微信）")
    @NotNull(message = "{bindType.notEmpty}", groups = {SaveGroup.class})
    @EnumValid(message = "{bindType.illegal}", clazz = BindTypeEnum.class, groups = {SaveGroup.class})
    private BindTypeEnum type;

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    private Long userId;
}
