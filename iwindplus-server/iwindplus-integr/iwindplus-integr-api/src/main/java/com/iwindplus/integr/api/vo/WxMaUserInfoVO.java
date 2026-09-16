/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 微信小程序用户信息视图对象.
 *
 * @author zengdegui
 * @since 2019/10/10
 */
@Schema(description = "微信小程序用户信息视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WxMaUserInfoVO implements Serializable {

    /**
     * 用户唯一标识.
     */
    @Schema(description = "用户唯一标识")
    private String openid;

    /**
     * 用户session key.
     */
    @Schema(description = "用户session key")
    private String sessionKey;

    /**
     * 昵称.
     */
    @Schema(description = "昵称")
    private String nickName;

    /**
     * 性别.
     */
    @Schema(description = "性别")
    private String gender;

    /**
     * 语言.
     */
    @Schema(description = "语言")
    private String language;

    /**
     * 城市.
     */
    @Schema(description = "城市")
    private String city;

    /**
     * 省份.
     */
    @Schema(description = "省份")
    private String province;

    /**
     * 国家.
     */
    @Schema(description = "国家")
    private String country;

    /**
     * 头像.
     */
    @Schema(description = "头像")
    private String avatarUrl;

    /**
     * 用户在开放平台的唯一标识符，不绑定开放平台不会返回这个字段
     */
    @Schema(description = "用户在开放平台的唯一标识符")
    private String unionId;

    /**
     * 水印数据.
     */
    @Schema(description = "水印数据")
    private WxWatermarkVO watermark;
}
