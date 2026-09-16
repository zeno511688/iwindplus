/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.interfaces.api;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.integr.api.WechatApi;
import com.iwindplus.integr.api.dto.WxMaGetQrCodeDTO;
import com.iwindplus.integr.api.dto.WxMaGetUserInfoDTO;
import com.iwindplus.integr.api.vo.WxMaPhoneNumberVO;
import com.iwindplus.integr.api.vo.WxMaUserInfoVO;
import com.iwindplus.integr.api.vo.WxMpUserInfoVO;
import com.iwindplus.integr.application.service.WechatApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class WechatApiImpl extends BaseController implements WechatApi {

    private final WechatApplicationService wechatService;

    @Override
    public ResultVO<String> getMpQrCode(String cfgCode, String callbackUrl, String state) {
        String result = this.wechatService.getMpQrCode(cfgCode, callbackUrl, state);
        return ResultVO.success(result);
    }

    @Override
    public ResultVO<WxMpUserInfoVO> getMpUserInfo(String cfgCode, String code) {
        WxMpUserInfoVO result = this.wechatService.getMpUserInfo(cfgCode, code);
        return ResultVO.success(result);
    }

    @Override
    public ResultVO<String> getMaQrCode(WxMaGetQrCodeDTO entity) {
        String result = this.wechatService.getMaQrCode(entity);
        return ResultVO.success(result);
    }

    @Override
    public ResultVO<WxMaPhoneNumberVO> getMaPhoneNumberInfo(String cfgCode, String code) {
        WxMaPhoneNumberVO result = this.wechatService.getMaPhoneNumberInfo(cfgCode, code);
        return ResultVO.success(result);
    }

    @Override
    public ResultVO<WxMaUserInfoVO> getMaUserInfo(WxMaGetUserInfoDTO entity) {
        WxMaUserInfoVO result = this.wechatService.getMaUserInfo(entity);
        return ResultVO.success(result);
    }
}
