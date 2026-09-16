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
 * 微信小程序手机信息视图对象.
 *
 * @author zengdegui
 * @since 2019/10/10
 */
@Schema(description = "微信小程序手机信息视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WxMaPhoneNumberVO implements Serializable {

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
     * 用户session key.
     */
    @Schema(description = "用户session key")
    private String sessionKey;

    /**
     * 手机号码.
     */
    @Schema(description = "手机号码")
    private String phoneNumber;

    /**
     * 纯手机号码.
     */
    @Schema(description = "纯手机号码")
    private String purePhoneNumber;

    /**
     * 国家代码.
     */
    @Schema(description = "国家代码")
    private String countryCode;

    /**
     * 水印数据.
     */
    @Schema(description = "水印数据")
    private WxWatermarkVO watermark;
}
