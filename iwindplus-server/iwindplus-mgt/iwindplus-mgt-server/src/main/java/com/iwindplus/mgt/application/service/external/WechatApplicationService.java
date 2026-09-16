/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.application.service.external;

import cn.hutool.core.net.url.UrlBuilder;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BaseEnum;
import com.iwindplus.base.domain.enums.UserSexEnum;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.util.CryptoUtil;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.integr.api.dto.WxMaGetQrCodeDTO;
import com.iwindplus.integr.api.dto.WxMaGetUserInfoDTO;
import com.iwindplus.integr.api.vo.WxMaPhoneNumberVO;
import com.iwindplus.integr.api.vo.WxMaUserInfoVO;
import com.iwindplus.integr.api.vo.WxMpUserInfoVO;
import com.iwindplus.integr.client.WechatClient;
import com.iwindplus.mgt.application.service.upms.user.UserExtendBindGrantApplicationService;
import com.iwindplus.mgt.application.service.upms.user.dto.UserExtendBindGrantSaveEditDTO;
import com.iwindplus.mgt.application.service.upms.user.dto.UserExtendBindGrantUserDTO;
import com.iwindplus.mgt.application.service.upms.user.vo.UserExtendBindGrantResultVO;
import com.iwindplus.mgt.common.constant.MgtConstant.WechatConstant;
import com.iwindplus.mgt.common.enums.BindTypeEnum;
import com.iwindplus.mgt.infrastructure.configuration.MgtProperty;
import com.iwindplus.mgt.infrastructure.configuration.MgtProperty.WechatConfig;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 微信业务层接口实现类.
 *
 * @author zengdegui
 * @since 2021/9/2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatApplicationService {

    private final MgtProperty property;
    private final UserExtendBindGrantApplicationService userExtendBindGrantApplicationService;
    private final WechatClient wechatClient;

    public String getMpQrCode() {
        final WechatConfig wechat = this.property.getWechat();
        final String cfgCode = wechat.getMpCode();
        final String callbackUrl = wechat.getMpCallbackUrl();
        final ResultVO<String> result = this.wechatClient.getMpQrCode(cfgCode, callbackUrl, null);
        result.errorThrow();
        return result.getBizData();
    }

    public void getMpQrCodeCallback(String code, HttpServletResponse response) {
        final WechatConfig wechat = this.property.getWechat();
        final String cfgCode = wechat.getMpCode();
        final ResultVO<WxMpUserInfoVO> result = this.wechatClient.getMpUserInfo(cfgCode, code);
        result.errorThrow();
        final WxMpUserInfoVO data = result.getBizData();
        log.info("WxMpUserInfoVO={}", data);
        final UserExtendBindGrantSaveEditDTO param = UserExtendBindGrantSaveEditDTO
            .builder()
            .openid(data.getOpenid())
            .unionId(data.getUnionId())
            .type(BindTypeEnum.MP)
            .build();
        final UserExtendBindGrantResultVO body = this.userExtendBindGrantApplicationService.saveOrEdit(param);
        final UserExtendBindGrantUserDTO userDTO = UserExtendBindGrantUserDTO.builder()
            .code(body.getCode())
            .bindFlag(body.getBindFlag())
            .nickName(data.getNickname())
            .sex(BaseEnum.fromValue(data.getSex(), UserSexEnum.class))
            .country(data.getCountry())
            .province(data.getProvince())
            .city(data.getCity())
            .avatar(data.getHeadImgUrl())
            .build();
        final String rawKey = JacksonUtil.toJsonStr(userDTO);
        final String state = CryptoUtil.encryptByBase64(rawKey);
        final String redirectUrl = UrlBuilder.ofHttp(wechat.getMpCallbackSuccessUrl())
            .addQuery(WechatConstant.STATE, state)
            .build();
        try {
            response.sendRedirect(redirectUrl);
        } catch (IOException ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
    }

    public String getMaQrCode(WxMaGetQrCodeDTO entity) {
        final WechatConfig wechat = this.property.getWechat();
        final String cfgCode = wechat.getMpCode();
        entity.setCode(cfgCode);
        final ResultVO<String> result = this.wechatClient.getMaQrCode(entity);
        result.errorThrow();
        return result.getBizData();
    }

    public UserExtendBindGrantResultVO getMaCodeByPhoneNumber(String code) {
        final WechatConfig wechat = this.property.getWechat();
        final String cfgCode = wechat.getMpCode();
        final ResultVO<WxMaPhoneNumberVO> result = this.wechatClient.getMaPhoneNumberInfo(cfgCode, code);
        result.errorThrow();
        final WxMaPhoneNumberVO data = result.getBizData();
        log.info("WechatMaPhoneNumberVO={}", data);
        final UserExtendBindGrantSaveEditDTO param = UserExtendBindGrantSaveEditDTO
            .builder()
            .openid(data.getOpenid())
            .unionId(data.getUnionId())
            .type(BindTypeEnum.MA)
            .mobile(data.getPurePhoneNumber())
            .build();
        return this.userExtendBindGrantApplicationService.saveOrEdit(param);
    }

    public void getMaCodeByUserInfo(WxMaGetUserInfoDTO entity, HttpServletResponse response) {
        final WechatConfig wechat = this.property.getWechat();
        final String cfgCode = wechat.getMpCode();
        entity.setCode(cfgCode);
        final ResultVO<WxMaUserInfoVO> result = this.wechatClient.getMaUserInfo(entity);
        result.errorThrow();
        final WxMaUserInfoVO data = result.getBizData();
        log.info("WechatMaUserInfoVO={}", data);
        final UserExtendBindGrantSaveEditDTO param = UserExtendBindGrantSaveEditDTO
            .builder()
            .openid(data.getOpenid())
            .unionId(data.getUnionId())
            .type(BindTypeEnum.MA)
            .build();
        final UserExtendBindGrantResultVO body = this.userExtendBindGrantApplicationService.saveOrEdit(param);
        final UserExtendBindGrantUserDTO userDTO = UserExtendBindGrantUserDTO.builder()
            .code(body.getCode())
            .bindFlag(body.getBindFlag())
            .sex(BaseEnum.fromValue(Integer.valueOf(data.getGender()), UserSexEnum.class))
            .country(data.getCountry())
            .province(data.getProvince())
            .city(data.getCity())
            .avatar(data.getAvatarUrl())
            .build();
        final String rawKey = JacksonUtil.toJsonStr(userDTO);
        final String state = CryptoUtil.encryptByBase64(rawKey);
        final String redirectUrl = UrlBuilder.ofHttp(wechat.getMaCallbackSuccessUrl())
            .addQuery(WechatConstant.STATE, state)
            .build();
        try {
            response.sendRedirect(redirectUrl);
        } catch (IOException ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
    }
}
