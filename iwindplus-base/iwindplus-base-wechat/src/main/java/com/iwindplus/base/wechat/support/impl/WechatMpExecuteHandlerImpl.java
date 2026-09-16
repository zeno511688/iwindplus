/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.wechat.support.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.wechat.domain.constant.WechatConstant;
import com.iwindplus.base.wechat.domain.property.WechatProperty;
import com.iwindplus.base.wechat.support.WechatMpExecuteHandler;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.redis.RedisTemplateWxRedisOps;
import me.chanjar.weixin.mp.api.WxMpQrcodeService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import me.chanjar.weixin.mp.config.impl.WxMpRedisConfigImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;

/**
 * 微信公众号相关业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/4/10
 */
@Slf4j
public class WechatMpExecuteHandlerImpl extends WxMpServiceImpl implements WechatMpExecuteHandler {

    /**
     * 配置.
     */
    private WechatProperty.MpConfig config;

    /**
     * 构造函数.
     *
     * @param config             微信公众号配置
     * @param stringRedisTemplate redis模板
     */
    public WechatMpExecuteHandlerImpl(WechatProperty.MpConfig config, StringRedisTemplate stringRedisTemplate) {
        this.config = config;
        if (Objects.nonNull(config) && CharSequenceUtil.isNotBlank(config.getAppId())
            && CharSequenceUtil.isNotBlank(config.getSecret())) {
            WxMpDefaultConfigImpl wxConfig;
            if (Boolean.TRUE.equals(config.getUseRedis())) {
                RedisTemplateWxRedisOps wxRedisOps = new RedisTemplateWxRedisOps(stringRedisTemplate);
                wxConfig = new WxMpRedisConfigImpl(wxRedisOps, WechatConstant.WECHAT_MP_PREFIX);
            } else {
                wxConfig = new WxMpDefaultConfigImpl();
            }
            wxConfig.setAppId(config.getAppId());
            wxConfig.setSecret(config.getSecret());
            wxConfig.setToken(config.getToken());
            wxConfig.setAesKey(config.getAesKey());
            this.setWxMpConfigStorage(wxConfig);
        }
    }

    @Override
    public WechatProperty.MpConfig getConfig() {
        return this.config;
    }

    @Override
    public void setConfig(WechatProperty.MpConfig config) {
        this.config = config;
    }

    @Override
    public boolean isHealthy() {
        return this.config != null && Boolean.TRUE.equals(this.config.getEnabled());
    }

    @Override
    public int getPriority() {
        return Optional.ofNullable(this.config)
            .map(WechatProperty.MpConfig::getPriority)
            .orElse(Integer.MAX_VALUE);
    }

    @Override
    public String getCode() {
        return this.config != null ? this.config.getCode() : null;
    }
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
