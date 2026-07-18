/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.wechat.service.impl;

import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.wechat.domain.constant.WechatConstant;
import com.iwindplus.base.wechat.service.WechatMpService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpQrcodeService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.http.HttpStatus;

/**
 * 微信公众号相关业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/4/10
 */
@Slf4j
public class WechatMpServiceImpl extends WxMpServiceImpl implements WechatMpService {
    @Override
    public WxOAuth2AccessToken getAccessToken(String code) {
        try {
            return this.getOAuth2Service().getAccessToken(code);
        } catch (WxErrorException ex) {
            log.error("WxErrorException, message={}", ex.getMessage(), ex);

            int errorCode = ex.getError().getErrorCode();
            if (errorCode == WechatConstant.INVALID_CODE) {
                throw new BizException(BizCodeEnum.INVALID_CODE);
            } else if (errorCode == WechatConstant.CODE_CAN_USE_ONCE) {
                throw new BizException(BizCodeEnum.CODE_CAN_USE_ONCE);
            } else if (errorCode == WechatConstant.FAILED) {
                throw new BizException(HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                throw new BizException(BizCodeEnum.WECHAT_MP_GRANT_ERROR);
            }
        }
    }

    @Override
    public WxOAuth2UserInfo getUserInfo(String code, String lang) {
        final WxOAuth2AccessToken accessToken = this.getAccessToken(code);
        try {
            return this.getOAuth2Service().getUserInfo(accessToken, lang);
        } catch (WxErrorException ex) {
            log.error("WxErrorException, message={}", ex.getMessage(), ex);

            throw new BizException(BizCodeEnum.GET_USER_INFO_ERROR);
        }
    }

    @Override
    public String getQrCode(String scene) {
        WxMpQrcodeService qrcodeService = this.getQrcodeService();
        try {
            WxMpQrCodeTicket wxMpQrCodeTicket = qrcodeService.qrCodeCreateLastTicket(scene);
            return qrcodeService.qrCodePictureUrl(wxMpQrCodeTicket.getTicket());
        } catch (WxErrorException ex) {
            log.error("WxErrorException, message={}", ex.getMessage(), ex);

            int errorCode = ex.getError().getErrorCode();
            if (errorCode == WechatConstant.FREQUENCY_LIMIT) {
                throw new BizException(BizCodeEnum.FREQUENCY_LIMIT);
            } else if (errorCode == WechatConstant.PAGE_ILLEGAL) {
                throw new BizException(BizCodeEnum.PAGE_ILLEGAL);
            } else {
                throw new BizException(BizCodeEnum.GET_QRCODE_ERROR);
            }
        }
    }

    @Override
    public boolean switchover(String appId) {
        if (super.switchover(appId)) {
            return true;
        } else {
            throw new BizException(BizCodeEnum.CONFIG_NOT_FOUND, new Object[]{appId});
        }
    }
}
