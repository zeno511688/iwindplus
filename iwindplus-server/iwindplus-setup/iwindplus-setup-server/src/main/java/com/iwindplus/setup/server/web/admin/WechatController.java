/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.setup.server.web.admin;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.domain.vo.system.ThirdBindGrantResultVO;
import com.iwindplus.setup.domain.dto.WechatMaGetQrCodeDTO;
import com.iwindplus.setup.server.service.WechatService;
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
@RequestMapping("admin/setup/wechat")
@Validated
@RequiredArgsConstructor
public class WechatController extends BaseController {

    private final WechatService wechatService;

    /**
     * 获取微信二维码.
     *
     * @param state 公众号配置编码
     * @return ResultVO < String>
     */
    @Operation(summary = "获取微信二维码")
    @GetMapping("getWechatMpQrCode")
    public ResultVO<String> getWechatMpQrCode(@RequestParam String state) {
        String data = this.wechatService.getWechatMpQrCode(state);
        return ResultVO.success(data);
    }

    /**
     * 微信扫码登录回调（由于获取不到手机，需要重定向至绑定手机页面）.
     *
     * @param code     code码
     * @param state    公众号配置编码
     * @param response 响应（重定向）
     */
    @Operation(summary = "微信扫码登录回调")
    @GetMapping("getWechatMpQrCodeCallback")
    public void getWechatMpQrCodeCallback(String code, String state, HttpServletResponse response) {
        log.info("微信扫码登录回调，code={}, state={}", code, state);
        this.wechatService.getWechatMpQrCodeCallback(code, state, response);
    }

    /**
     * 获取微信小程序手机号授权登录绑定编码.
     *
     * @param code  code码
     * @param state 小程序配置编码
     * @return ResultVO < ThirdBindGrantResultVO>
     */
    @Operation(summary = "获取微信小程序手机号授权登录绑定编码")
    @GetMapping("getWechatMaCodeByMobile")
    public ResultVO<ThirdBindGrantResultVO> getWechatMaCodeByMobile(@RequestParam String code, @RequestParam String state) {
        ThirdBindGrantResultVO data = this.wechatService.getWechatMaCodeByMobile(code, state);
        return ResultVO.success(data);
    }

    /**
     * 获取微信小程序用户授权登录绑定编码（由于获取不到手机，需要重定向至绑定手机页面）.
     *
     * @param code          code码
     * @param state         小程序配置编码
     * @param rawData       用户原始数据字符串
     * @param signature     用户信息签名
     * @param encryptedData 加密用户数据
     * @param iv            初始向量
     * @param response      响应（重定向）
     */
    @Operation(summary = "获取微信小程序用户授权登录绑定编码")
    @GetMapping("getWechatMaCodeByUserInfo")
    public void getWechatMaCodeByUserInfo(
        @RequestParam String code,
        @RequestParam String state,
        @RequestParam String rawData,
        @RequestParam String signature,
        @RequestParam String encryptedData,
        @RequestParam String iv,
        HttpServletResponse response) {
        this.wechatService.getWechatMaCodeByUserInfo(code, state, rawData, signature, encryptedData, iv, response);
    }

    /**
     * 获取微信小程序生产二维码.
     *
     * @param entity 对象
     * @return ResultVO < String>
     */
    @Operation(summary = "获取微信小程序生产二维码")
    @PostMapping("getWechatMaQrCode")
    public ResultVO<String> getWechatMaQrCode(@RequestBody @Validated WechatMaGetQrCodeDTO entity) {
        String data = this.wechatService.getWechatMaQrCode(entity);
        return ResultVO.success(data);
    }
}
