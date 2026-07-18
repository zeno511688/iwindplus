/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.server.service;

import com.iwindplus.mgt.domain.vo.system.ThirdBindGrantResultVO;
import com.iwindplus.setup.domain.dto.WechatMaGetQrCodeDTO;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 微信业务层接口类.
 *
 * @author zengdegui
 * @since 2021/7/20
 */
public interface WechatService {

    /**
     * 微信扫码登录.
     *
     * @param state 公众号配置编码
     * @return String
     */
    String getWechatMpQrCode(String state);

    /**
     * 微信扫码登录回调（由于获取不到手机，需要重定向至绑定手机页面）.
     *
     * @param code     code
     * @param state    公众号配置编码
     * @param response 响应
     */
    void getWechatMpQrCodeCallback(String code, String state, HttpServletResponse response);

    /**
     * 获取微信小程序手机号授权登录绑定编码.
     *
     * @param code  code码
     * @param state 小程序配置编码
     * @return ThirdBindGrantResultVO
     */
    ThirdBindGrantResultVO getWechatMaCodeByMobile(String code, String state);

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
     * @param response      响应
     */
    void getWechatMaCodeByUserInfo(String code, String state, String rawData, String signature, String encryptedData, String iv,
        HttpServletResponse response);

    /**
     * 获取微信小程序生产二维码.
     *
     * @param entity 对象
     * @return String
     */
    String getWechatMaQrCode(WechatMaGetQrCodeDTO entity);
}
