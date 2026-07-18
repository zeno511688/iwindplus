/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.wechat.service;

import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;

/**
 * 微信公众号相关业务层接口类.
 *
 * @author zengdegui
 * @since 2020/4/10
 */
public interface WechatMpService extends WxMpService {
    /**
     * 用code换取oauth2的access token
     *
     * @param code code码
     * @return WxOAuth2AccessToken
     */
    WxOAuth2AccessToken getAccessToken(String code);

    /**
     * 获取用户信息.
     *
     * @param code code码
     * @param lang code码
     * @return WxOAuth2UserInfo
     */
    WxOAuth2UserInfo getUserInfo(String code, String lang);

    /**
     * 获取微信公众号二维码.
     *
     * @param scene 二维码隐藏的内容
     * @return String
     */
    String getQrCode(String scene);
}
