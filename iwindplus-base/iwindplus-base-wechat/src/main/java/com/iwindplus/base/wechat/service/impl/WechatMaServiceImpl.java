/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.wechat.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaUserService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.bean.WxMaCodeLineColor;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64Encoder;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.wechat.domain.constant.WechatConstant;
import com.iwindplus.base.wechat.domain.vo.WechatMaPhoneNumberVO;
import com.iwindplus.base.wechat.domain.vo.WechatMaUserInfoVO;
import com.iwindplus.base.wechat.service.WechatMaService;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.http.HttpStatus;

/**
 * 微信相关业务接口实现类.
 *
 * @author zengdegui
 * @since 2019/10/10
 */
@Slf4j
public class WechatMaServiceImpl extends WxMaServiceImpl implements WechatMaService {
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
    public WechatMaUserInfoVO getUserInfo(String code, String rawData, String signature, String encryptedData, String iv) {
        WxMaJscode2SessionResult sessionInfo = this.getSessionInfo(code);
        final WxMaUserService userService = this.getUserService();
        boolean checkUserInfo = userService.checkUserInfo(sessionInfo.getSessionKey(), rawData, signature);
        if (checkUserInfo) {
            // 解密用户敏感数据
            WxMaUserInfo userInfo = userService.getUserInfo(sessionInfo.getSessionKey(), encryptedData, iv);
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
    public String getQrCode(String scene, String page, Boolean checkPath, String envVersion, Integer width, Boolean isHyaline) {
        byte[] data;
        try {
            data = this.getQrcodeService().createWxaCodeUnlimitBytes(scene, page,
                    Optional.ofNullable(checkPath).orElse(Boolean.TRUE),
                    Optional.ofNullable(envVersion).orElse("release"),
                    Optional.ofNullable(width).orElse(430),
                    Boolean.TRUE, new WxMaCodeLineColor(),
                    Optional.ofNullable(isHyaline).orElse(Boolean.FALSE));
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
