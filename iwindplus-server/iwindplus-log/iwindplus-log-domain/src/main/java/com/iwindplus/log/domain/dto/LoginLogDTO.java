/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.dto;

import com.iwindplus.base.domain.dto.DbBaseTwoDTO;
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
 * 登录日志.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Schema(description = "登录日志对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LoginLogDTO extends DbBaseTwoDTO {

    /**
     * 请求唯一标识.
     */
    @Schema(description = "请求唯一标识")
    @Length(max = 100, message = "{requestId.length}", groups = {SaveGroup.class, EditGroup.class})
    private String requestId;

    /**
     * 跟踪唯一标识.
     */
    @Schema(description = "跟踪唯一标识")
    @Length(max = 100, message = "{bizTraceId.length}", groups = {SaveGroup.class, EditGroup.class})
    private String bizTraceId;

    /**
     * 模块名称.
     */
    @Schema(description = "模块名称")
    @NotBlank(message = "{moduleName.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{moduleName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String moduleName;

    /**
     * 模块描述.
     */
    @Schema(description = "模块描述")
    @NotBlank(message = "{moduleDesc.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 255, message = "{moduleDesc.length}", groups = {SaveGroup.class, EditGroup.class})
    private String moduleDesc;

    /**
     * 平台名称.
     */
    @Schema(description = "平台名称")
    @Length(max = 100, message = "{platformName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String platformName;

    /**
     * 系统名称.
     */
    @Schema(description = "系统名称")
    @Length(max = 100, message = "{osName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String osName;

    /**
     * 系统版本.
     */
    @Schema(description = "系统版本")
    @Length(max = 100, message = "{osVersion.length}", groups = {SaveGroup.class, EditGroup.class})
    private String osVersion;

    /**
     * 浏览器名称.
     */
    @Schema(description = "浏览器名称")
    @Length(max = 100, message = "{browserName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String browserName;

    /**
     * 浏览器版本.
     */
    @Schema(description = "浏览器版本")
    @Length(max = 100, message = "{browserVersion.length}", groups = {SaveGroup.class, EditGroup.class})
    private String browserVersion;

    /**
     * 设备号.
     */
    @Schema(description = "设备号")
    @Length(max = 100, message = "{deviceNumber.length}", groups = {SaveGroup.class, EditGroup.class})
    private String deviceNumber;

    /**
     * 设备版本.
     */
    @Schema(description = "设备版本")
    @Length(max = 100, message = "{deviceVersion.length}", groups = {SaveGroup.class, EditGroup.class})
    private String deviceVersion;

    /**
     * 设备指纹.
     */
    @Schema(description = "设备指纹")
    @Length(max = 100, message = "{deviceFingerprint.length}", groups = {SaveGroup.class, EditGroup.class})
    private String deviceFingerprint;

    /**
     * 请求ip.
     */
    @Schema(description = "请求ip")
    @Length(max = 100, message = "{ip.length}", groups = {SaveGroup.class, EditGroup.class})
    private String ip;

    /**
     * 省份.
     */
    @Schema(description = "省份")
    @Length(max = 100, message = "{province.length}", groups = {SaveGroup.class, EditGroup.class})
    private String province;

    /**
     * 城市.
     */
    @Schema(description = "城市")
    @Length(max = 100, message = "{city.length}", groups = {SaveGroup.class, EditGroup.class})
    private String city;

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
}
