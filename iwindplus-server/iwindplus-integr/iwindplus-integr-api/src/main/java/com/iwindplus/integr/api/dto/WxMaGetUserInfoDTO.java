/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 微信小程序用户授权登录数据传输对象.
 *
 * @author zengdegui
 * @since 2026/9/4
 */
@Schema(description = "微信小程序用户授权登录数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WxMaGetUserInfoDTO implements Serializable {

    /**
     * 小程序配置编码.
     */
    @Schema(description = "小程序配置编码")
    private String code;

    /**
     * jsCode – 登录时获取的code.
     */
    @Schema(description = "登录时获取的code")
    @NotBlank(message = "{jsCode.notEmpty}")
    private String jsCode;

    /**
     * 用户原始数据字符串.
     */
    @Schema(description = "用户原始数据字符串")
    @NotBlank(message = "{rawData.notEmpty}")
    private String rawData;

    /**
     * 用户信息签名.
     */
    @Schema(description = "用户信息签名")
    @NotBlank(message = "{signature.notEmpty}")
    private String signature;

    /**
     * 加密用户数据.
     */
    @Schema(description = "加密用户数据")
    @NotBlank(message = "{encryptedData.notEmpty}")
    private String encryptedData;

    /**
     * 初始向量.
     */
    @Schema(description = "初始向量")
    @NotBlank(message = "{iv.notEmpty}")
    private String iv;
}
