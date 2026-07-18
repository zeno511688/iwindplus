/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.domain.dto;

import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 短信模板数据传输对象.
 *
 * @author zengdegui
 * @since 2020/4/27
 */
@Schema(description = "短信模板数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SmsTplDTO extends DbVersionBaseDTO {

    /**
     * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
     */
    @Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
    private EnableStatusEnum status;

    /**
     * 编码.
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    @NotBlank(message = "{templateName.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 50, message = "{templateName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String name;

    /**
     * 短信签名（可选）.
     */
    @Schema(description = "短信签名")
    @Length(max = 100, message = "{signName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String signName;

    /**
     * 短信模板内容.
     */
    @Schema(description = "短信模板内容")
    @NotBlank(message = "{templateContent.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 5000, message = "{templateContent.length}", groups = {SaveGroup.class, EditGroup.class})
    private String templateContent;

    /**
     * 短信验证码有效时间（单位：分钟）.
     */
    @Schema(description = "短信验证码有效时间（单位：分钟）")
    private Integer captchaTimeout;

    /**
     * 短信验证码位数（默认6位）.
     */
    @Schema(description = "短信验证码位数")
    private Integer captchaLength;

    /**
     * 限制手机每天次数（默认20次）.
     */
    @Schema(description = "限制手机每天次数")
    private Integer limitCountDay;

    /**
     * 限制每小时次数（默认5次）.
     */
    @Schema(description = "限制每小时次数")
    private Integer limitCountHour;

    /**
     * 限制手机每分钟次数（默认1次）.
     */
    @Schema(description = "限制手机每分钟次数")
    private Integer limitCountMinute;

    /**
     * 是否内置（false：否，true：是）.
     */
    @Schema(description = "是否内置（false：否，true：是）")
    private Boolean buildInFlag;

    /**
     * 配置主键.
     */
    @Schema(description = "配置主键")
    private Long configId;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}
