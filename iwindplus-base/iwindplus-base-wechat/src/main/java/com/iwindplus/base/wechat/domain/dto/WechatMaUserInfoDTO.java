/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.wechat.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 微信小程序获取用户信息数据传输对象.
 *
 * @author zengdegui
 * @since 2019/7/16
 */
@Schema(description = "微信小程序获取用户信息数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WechatMaUserInfoDTO implements Serializable {
    /**
     * code码.
     */
    @Schema(description = "code码")
    @NotBlank(message = "{code.notEmpty}")
    private String code;

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
}
