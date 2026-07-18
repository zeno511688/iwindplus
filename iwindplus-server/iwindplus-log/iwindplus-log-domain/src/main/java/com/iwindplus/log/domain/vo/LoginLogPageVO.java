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
 * 登录日志分页视图对象.
 *
 * @author zengdegui
 * @since 2019/7/16
 */
@Schema(description = "登录日志分页视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LoginLogPageVO extends DbBaseTwoVO {

    /**
     * 请求唯一标识.
     */
    @Schema(description = "请求唯一标识")
    private String requestId;

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
     * 浏览器名称.
     */
    @Schema(description = "浏览器名称")
    private String browserName;

    /**
     * 跟踪唯一标识.
     */
    @Schema(description = "跟踪唯一标识")
    private String bizTraceId;

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
