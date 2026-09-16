/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.wechat.support.impl;

import cn.binarywang.wx.miniapp.api.WxMaUserService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.bean.WxMaCodeLineColor;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaRedisBetterConfigImpl;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.wechat.domain.constant.WechatConstant;
import com.iwindplus.base.wechat.domain.dto.WechatMaQrCodeDTO;
import com.iwindplus.base.wechat.domain.dto.WechatMaUserInfoDTO;
import com.iwindplus.base.wechat.domain.property.WechatProperty;
import com.iwindplus.base.wechat.domain.vo.WechatMaPhoneNumberVO;
import com.iwindplus.base.wechat.domain.vo.WechatMaUserInfoVO;
import com.iwindplus.base.wechat.support.WechatMaExecuteHandler;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.redis.RedisTemplateWxRedisOps;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;

/**
 * 微信小程序相关业务层接口实现类.
 *
 * @author zengdegui
 * @since 2019/10/10
 */
@Slf4j
public class WechatMaExecuteHandlerImpl extends WxMaServiceImpl implements WechatMaExecuteHandler {

    /**
     * 配置.
     */
    private WechatProperty.MaConfig config;

    /**
     * 构造函数，绑定单个小程序配置.
     *
     * @param config 小程序配置
     * @param stringRedisTemplate redis模板
     */
    public WechatMaExecuteHandlerImpl(WechatProperty.MaConfig config, StringRedisTemplate stringRedisTemplate) {
        this.config = config;
        if (config != null && CharSequenceUtil.isNotBlank(config.getAppId())
            && CharSequenceUtil.isNotBlank(config.getSecret())) {
            WxMaDefaultConfigImpl wxConfig;
            if (Boolean.TRUE.equals(config.getUseRedis())) {
                RedisTemplateWxRedisOps wxRedisOps = new RedisTemplateWxRedisOps(stringRedisTemplate);
                wxConfig = new WxMaRedisBetterConfigImpl(wxRedisOps, WechatConstant.WECHAT_MA_PREFIX);
            } else {
                wxConfig = new WxMaDefaultConfigImpl();
            }
            wxConfig.setAppid(config.getAppId());
            wxConfig.setSecret(config.getSecret());
            wxConfig.setToken(config.getToken());
            wxConfig.setAesKey(config.getAesKey());
            wxConfig.setMsgDataFormat(config.getMsgDataFormat());
            this.setWxMaConfig(wxConfig);
        }
    }

    @Override
    public WechatProperty.MaConfig getConfig() {
        return this.config;
    }

    @Override
    public void setConfig(WechatProperty.MaConfig config) {
        this.config = config;
    }

    @Override
    public boolean isHealthy() {
        return this.config != null && Boolean.TRUE.equals(this.config.getEnabled());
    }

    @Override
    public int getPriority() {
        return Optional.ofNullable(this.config)
            .map(WechatProperty.MaConfig::getPriority)
            .orElse(Integer.MAX_VALUE);
    }

    @Override
    public String getCode() {
        return this.config != null ? this.config.getCode() : null;
    }

    @Override
    public WxMaJscode2SessionResult getSessionInfo(String code) {
        try {
            return this.getUserService().getSessionInfo(code);
        } catch (WxErrorException ex) {
            log.error("WxErrorException, message={}", ex.getMessage(), ex);

            int errorCode = ex.getError().getErrorCode();
            if (errorCode == WechatConstant.INVALID_CODE) {
                throw new BizException(BizCodeEnum.INVALID_CODE);
            } else if (errorCode == WechatConstant.WECHAT_MA_FREQUENCY_LIMIT) {
                throw new BizException(BizCodeEnum.FREQUENCY_LIMIT);
            } else if (errorCode == WechatConstant.CODE_CAN_USE_ONCE) {
                throw new BizException(BizCodeEnum.CODE_CAN_USE_ONCE);
            } else if (errorCode == WechatConstant.HIGH_RISK_USER) {
                throw new BizException(BizCodeEnum.HIGH_RISK_USER);
            } else if (errorCode == WechatConstant.FAILED) {
                throw new BizException(HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                throw new BizException(BizCodeEnum.WECHAT_MA_GRANT_ERROR);
            }
        }
    }

    @Override
    public WechatMaPhoneNumberVO getPhoneNumberInfo(String code) {
        WxMaJscode2SessionResult sessionInfo = this.getSessionInfo(code);
        WxMaPhoneNumberInfo phoneNoInfo;
        try {
            // 解密用户手机号信息
            phoneNoInfo = this.getUserService().getPhoneNumber(code);
        } catch (WxErrorException ex) {
            log.error("WxErrorException, message={}", ex.getMessage(), ex);

            throw new BizException(BizCodeEnum.GET_PHONE_NUMBER_ERROR);
        }
        WechatMaPhoneNumberVO data = BeanUtil.copyProperties(phoneNoInfo, WechatMaPhoneNumberVO.class);
        data.setSessionKey(sessionInfo.getSessionKey());
        data.setOpenid(sessionInfo.getOpenid());
        data.setUnionId(sessionInfo.getUnionid());
        return data;
    }

    @Override
    public WechatMaUserInfoVO getUserInfo(WechatMaUserInfoDTO entity) {
        WxMaJscode2SessionResult sessionInfo = this.getSessionInfo(entity.getJsCode());
        final WxMaUserService userService = this.getUserService();
        boolean checkUserInfo = userService.checkUserInfo(sessionInfo.getSessionKey(), entity.getRawData(), entity.getSignature());
        if (checkUserInfo) {
            // 解密用户敏感数据
            WxMaUserInfo userInfo = userService.getUserInfo(sessionInfo.getSessionKey(), entity.getEncryptedData(), entity.getIv());
            if (Objects.nonNull(userInfo)) {
                WechatMaUserInfoVO data = BeanUtil.copyProperties(userInfo, WechatMaUserInfoVO.class);
                data.setSessionKey(sessionInfo.getSessionKey());
                data.setOpenid(sessionInfo.getOpenid());
                data.setUnionId(sessionInfo.getUnionid());
                return data;
            }
        }
        throw new BizException(BizCodeEnum.GET_USER_INFO_ERROR);
    }

    @Override
    public String getQrCode(WechatMaQrCodeDTO entity) {
        byte[] data;
        try {
            data = this.getQrcodeService().createWxaCodeUnlimitBytes(entity.getScene(), entity.getPage(),
                    Optional.ofNullable(entity.getCheckPath()).orElse(Boolean.TRUE),
                    Optional.ofNullable(entity.getEnvVersion()).orElse("release"),
                    Optional.ofNullable(entity.getWidth()).orElse(430),
                    Boolean.TRUE, new WxMaCodeLineColor(),
                    Optional.ofNullable(entity.getHyaline()).orElse(Boolean.FALSE));
        } catch (WxErrorException ex) {
            log.error("WxErrorException, message={}", ex.getMessage(), ex);

            int errorCode = ex.getError().getErrorCode();
            if (errorCode == WechatConstant.WECHAT_MA_FREQUENCY_LIMIT) {
                throw new BizException(BizCodeEnum.FREQUENCY_LIMIT);
            } else if (errorCode == WechatConstant.PAGE_ILLEGAL) {
                throw new BizException(BizCodeEnum.PAGE_ILLEGAL);
            } else {
                throw new BizException(BizCodeEnum.GET_QRCODE_ERROR);
            }
        }
        return Base64Encoder.encode(data);
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
