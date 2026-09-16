/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.interfaces.controller.external;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.integr.api.dto.WxMaGetQrCodeDTO;
import com.iwindplus.integr.api.dto.WxMaGetUserInfoDTO;
import com.iwindplus.mgt.application.service.external.WechatApplicationService;
import com.iwindplus.mgt.application.service.upms.user.vo.UserExtendBindGrantResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "微信接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/wechat")
@Validated
@RequiredArgsConstructor
public class WechatController extends BaseController {

    private final WechatApplicationService wechatApplicationService;

    /**
     * 获取微信二维码.
     *
     * @return ResultVO < String>
     */
    @Operation(summary = "获取微信二维码")
    @GetMapping("getMpQrCode")
    public ResultVO<String> getMpQrCode() {
        final String data = this.wechatApplicationService.getMpQrCode();
        return ResultVO.success(data);
    }

    /**
     * 获取微信扫码登录回调，重定向.
     *
     * @param code     code码
     * @param response 响应（重定向）
     */
    @Operation(summary = "微信扫码登录回调")
    @GetMapping("getMpQrCodeCallback")
    public void getMpQrCodeCallback(@RequestParam String code, HttpServletResponse response) {
        log.info("微信扫码登录回调，code={}", code);
        this.wechatApplicationService.getMpQrCodeCallback(code, response);
    }

    /**
     * 获取微信小程序生产二维码.
     *
     * @param entity 对象
     * @return ResultVO < String>
     */
    @Operation(summary = "获取微信小程序生产二维码")
    @PostMapping("getMaQrCode")
    public ResultVO<String> getMaQrCode(@RequestBody @Validated WxMaGetQrCodeDTO entity) {
        String data = this.wechatApplicationService.getMaQrCode(entity);
        return ResultVO.success(data);
    }

    /**
     * 获取微信小程序手机号授权登录绑定编码.
     *
     * @param code code码
     * @return ResultVO < UserExtendBindGrantResultVO>
     */
    @Operation(summary = "获取微信小程序手机号授权登录绑定编码")
    @GetMapping("getMaCodeByPhoneNumber")
    public ResultVO<UserExtendBindGrantResultVO> getMaCodeByPhoneNumber(@RequestParam String code) {
        UserExtendBindGrantResultVO data = this.wechatApplicationService.getMaCodeByPhoneNumber(code);
        return ResultVO.success(data);
    }

    /**
     * 获取微信小程序用户授权登录绑定编码，重定向.
     *
     * @param entity   用户授权登录数据传输对象
     * @param response 响应（重定向）
     */
    @Operation(summary = "获取微信小程序用户授权登录绑定编码")
    @GetMapping("getMaCodeByUserInfo")
    public void getMaCodeByUserInfo(@Validated WxMaGetUserInfoDTO entity, HttpServletResponse response) {
        this.wechatApplicationService.getMaCodeByUserInfo(entity, response);
    }
}
