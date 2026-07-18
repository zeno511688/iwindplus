/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.system;

import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import com.iwindplus.mgt.domain.enums.BindTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 第三方绑定授权视图对象.
 *
 * @author zengdegui
 * @since 2019/7/16
 */
@Schema(description = "第三方绑定授权视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdBindGrantVO extends DbVersionBaseVO {
    /**
     * 编码.
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 用户唯一标识.
     */
    @Schema(description = "用户唯一标识")
    private String openid;

    /**
     * 用户在开放平台的唯一标识符.
     */
    @Schema(description = "用户在开放平台的唯一标识符")
    private String unionId;

    /**
     * 类型（MP：微信公众号，MA：微信小程序，CP：企业微信）.
     */
    @Schema(description = "类型（MP：微信公众号，MA：微信小程序，CP：企业微信）")
    private BindTypeEnum type;

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    private Long userId;

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

    /**
     * 应用名称.
     */
    @Schema(description = "应用名称")
    private String appName;

    /**
     * 应用简称.
     */
    @Schema(description = "应用简称")
    private String appAbbr;
}
