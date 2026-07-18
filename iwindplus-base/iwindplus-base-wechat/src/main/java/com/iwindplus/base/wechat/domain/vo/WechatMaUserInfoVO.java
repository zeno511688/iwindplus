/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.wechat.domain.vo;

import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 微信小程序用户信息视图对象.
 *
 * @author zengdegui
 * @since 2019/10/10
 */
@Schema(description = "微信小程序用户信息视图对象")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class WechatMaUserInfoVO extends WxMaUserInfo {
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
}
