/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.api;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.integr.api.dto.WxMaGetQrCodeDTO;
import com.iwindplus.integr.api.dto.WxMaGetUserInfoDTO;
import com.iwindplus.integr.api.vo.WxMaPhoneNumberVO;
import com.iwindplus.integr.api.vo.WxMaUserInfoVO;
import com.iwindplus.integr.api.vo.WxMpUserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 微信相关接口.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
public interface WechatApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/wechat/";

    /**
     * 获取微信扫码登录二维码地址.
     *
     * @param cfgCode     公众号配置编码
     * @param callbackUrl 回调地址
     * @param state       扩展参数（重定向会原样返回）
     * @return ResultVO<String>
     */
    @Operation(summary = "获取微信扫码登录二维码地址")
    @GetMapping(API_PREFIX + "getMpQrCode")
    ResultVO<String> getMpQrCode(
        @RequestParam(value = "cfgCode") String cfgCode,
        @RequestParam(value = "callbackUrl") String callbackUrl,
        @RequestParam(value = "state")String state);

    /**
     * 获取微信公众号用户信息.
     *
     * @param cfgCode 公众号配置编码
     * @param code    code码
     * @return ResultVO<WxMpUserInfoVO>
     */
    @GetMapping(API_PREFIX + "getMpUserInfo")
    ResultVO<WxMpUserInfoVO> getMpUserInfo(
        @RequestParam(value = "cfgCode") String cfgCode,
        @RequestParam(value = "code") String code);

    /**
     * 获取微信小程序生产二维码.
     *
     * @param entity 对象
     * @return ResultVO<String>
     */
    @PostMapping(API_PREFIX + "getMaQrCode")
    ResultVO<String> getMaQrCode(@RequestBody @Validated WxMaGetQrCodeDTO entity);

    /**
     * 获取微信小程序用户信息.
     *
     * @param cfgCode 小程序配置编码
     * @param code    code码
     * @return ResultVO<WxMaPhoneNumberVO>
     */
    @GetMapping(API_PREFIX + "getMaPhoneNumberInfo")
    ResultVO<WxMaPhoneNumberVO> getMaPhoneNumberInfo(
        @RequestParam(value = "cfgCode") String cfgCode,
        @RequestParam(value = "code") String code);

    /**
     * 获取微信小程序用户信息.
     *
     * @param entity 对象
     * @return ResultVO<WxMaUserInfoVO>
     */
    @PostMapping(API_PREFIX + "getMaUserInfo")
    ResultVO<WxMaUserInfoVO> getMaUserInfo(@RequestBody @Validated WxMaGetUserInfoDTO entity);
}
