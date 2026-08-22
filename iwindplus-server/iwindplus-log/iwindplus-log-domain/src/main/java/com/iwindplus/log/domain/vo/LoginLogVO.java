/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.vo;

import com.iwindplus.base.domain.vo.DbBaseTwoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 登录日志详情视图对象.
 *
 * @author zengdegui
 * @since 2019/7/16
 */
@Schema(description = "登录日志详情视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LoginLogVO extends DbBaseTwoVO {

    /**
     * 请求唯一标识.
     */
    @Schema(description = "请求唯一标识")
    private String requestId;

    /**
     * 跟踪唯一标识.
     */
    @Schema(description = "跟踪唯一标识")
    private String bizTraceId;

    /**
     * 模块名称.
     */
    @Schema(description = "模块名称")
    private String moduleName;

    /**
     * 模块描述.
     */
    @Schema(description = "模块描述")
    private String moduleDesc;

    /**
     * 平台名称.
     */
    @Schema(description = "平台名称")
    private String platformName;

    /**
     * 系统名称.
     */
    @Schema(description = "系统名称")
    private String osName;

    /**
     * 系统版本.
     */
    @Schema(description = "系统版本")
    private String osVersion;

    /**
     * 浏览器名称.
     */
    @Schema(description = "浏览器名称")
    private String browserName;

    /**
     * 浏览器版本.
     */
    @Schema(description = "浏览器版本")
    private String browserVersion;

    /**
     * 设备号.
     */
    @Schema(description = "设备号")
    private String deviceNumber;

    /**
     * 设备版本.
     */
    @Schema(description = "设备版本")
    private String deviceVersion;

    /**
     * 设备指纹.
     */
    @Schema(description = "设备指纹")
    private String deviceFingerprint;

    /**
     * ip.
     */
    @Schema(description = "ip")
    private String ip;

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
     * 工号
     */
    @Schema(description = "工号")
    private String jobNumber;

    /**
     * 手机.
     */
    @Schema(description = "手机")
    private String mobile;
}
