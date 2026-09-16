/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.application.service;

import cn.hutool.core.bean.BeanUtil;
import com.iwindplus.base.wechat.domain.dto.WechatMaQrCodeDTO;
import com.iwindplus.base.wechat.domain.dto.WechatMaUserInfoDTO;
import com.iwindplus.base.wechat.domain.vo.WechatMaPhoneNumberVO;
import com.iwindplus.base.wechat.domain.vo.WechatMaUserInfoVO;
import com.iwindplus.base.wechat.factory.WechatMaExecuteHandlerFactory;
import com.iwindplus.base.wechat.factory.WechatMpExecuteHandlerFactory;
import com.iwindplus.base.wechat.support.WechatMaExecuteHandler;
import com.iwindplus.base.wechat.support.WechatMpExecuteHandler;
import com.iwindplus.integr.api.dto.WxMaGetQrCodeDTO;
import com.iwindplus.integr.api.dto.WxMaGetUserInfoDTO;
import com.iwindplus.integr.api.vo.WxMaPhoneNumberVO;
import com.iwindplus.integr.api.vo.WxMaUserInfoVO;
import com.iwindplus.integr.api.vo.WxMpUserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 微信业务层接口类.
 *
 * @author zengdegui
 * @since 2021/7/20
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class WechatApplicationService {

    private final WechatMaExecuteHandlerFactory wechatMaExecuteHandlerFactory;
    private final WechatMpExecuteHandlerFactory wechatMpExecuteHandlerFactory;

    /**
     * 获取微信扫码登录二维码地址.
     *
     * @param cfgCode     公众号配置编码
     * @param callbackUrl 回调地址
     * @param state       扩展参数（重定向会原样返回）
     * @return String
     */
    public String getMpQrCode(String cfgCode, String callbackUrl, String state) {
        final WechatMpExecuteHandler handler = this.wechatMpExecuteHandlerFactory.getHandler(cfgCode);
        return handler.buildQrConnectUrl(callbackUrl, WxConsts.QrConnectScope.SNSAPI_LOGIN, state);
    }

    /**
     * 获取微信公众号用户信息.
     *
     * @param cfgCode 公众号配置编码
     * @param code    code码
     * @return WxMpUserInfoVO
     */
    public WxMpUserInfoVO getMpUserInfo(String cfgCode, String code) {
        final WechatMpExecuteHandler handler = this.wechatMpExecuteHandlerFactory.getHandler(cfgCode);
        final WxOAuth2UserInfo data = handler.getUserInfo(code, null);
        return BeanUtil.copyProperties(data, WxMpUserInfoVO.class);
    }

    /**
     * 获取微信小程序生产二维码.
     *
     * @param entity 对象
     * @return String
     */
    public String getMaQrCode(WxMaGetQrCodeDTO entity) {
        final WechatMaExecuteHandler handler = this.wechatMaExecuteHandlerFactory.getHandler(entity.getCode());
        final WechatMaQrCodeDTO wechatMaQrCodeDTO = BeanUtil.copyProperties(entity, WechatMaQrCodeDTO.class);
        return handler.getQrCode(wechatMaQrCodeDTO);
    }

    /**
     * 获取微信小程序手机号信息.
     *
     * @param cfgCode 小程序配置编码
     * @param code    code码
     * @return WxMaPhoneNumberVO
     */
    public WxMaPhoneNumberVO getMaPhoneNumberInfo(String cfgCode, String code) {
        final WechatMaExecuteHandler handler = this.wechatMaExecuteHandlerFactory.getHandler(cfgCode);
        final WechatMaPhoneNumberVO data = handler.getPhoneNumberInfo(code);
        return BeanUtil.copyProperties(data, WxMaPhoneNumberVO.class);
    }

    /**
     * 获取微信小程序用户信息.
     *
     * @param entity 对象
     * @return WxMaUserInfoVO
     */
    public WxMaUserInfoVO getMaUserInfo(WxMaGetUserInfoDTO entity) {
        final WechatMaExecuteHandler handler = this.wechatMaExecuteHandlerFactory.getHandler(entity.getCode());
        final WechatMaUserInfoDTO wechatMaUserInfoDTO = BeanUtil.copyProperties(entity, WechatMaUserInfoDTO.class);
        final WechatMaUserInfoVO data = handler.getUserInfo(wechatMaUserInfoDTO);
        return BeanUtil.copyProperties(data, WxMaUserInfoVO.class);
    }

}
