/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.wechat.service;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.iwindplus.base.wechat.domain.vo.WechatMaPhoneNumberVO;
import com.iwindplus.base.wechat.domain.vo.WechatMaUserInfoVO;

/**
 * 微信小程序相关业务层接口类.
 *
 * @author zengdegui
 * @since 2019/10/10
 */
public interface WechatMaService extends WxMaService {
    /**
     * 获取登录后的session信息.
     *
     * @param code code码
     * @return WxMaJscode2SessionResult
     */
    WxMaJscode2SessionResult getSessionInfo(String code);

    /**
     * 获取手机号信息.
     *
     * @param code 微信小程序code码
     * @return WechatMaPhoneNumberVO
     */
    WechatMaPhoneNumberVO getPhoneNumberInfo(String code);

    /**
     * 获取用户信息.
     *
     * @param code          code码
     * @param rawData       用户原始数据字符串
     * @param signature     用户信息签名
     * @param encryptedData 加密用户数据
     * @param iv            初始向量
     * @return WechatMaUserInfoVO
     */
    WechatMaUserInfoVO getUserInfo(String code, String rawData, String signature, String encryptedData, String iv);

    /**
     * 获取小程序二维码.
     *
     * @param scene      – 最大32个可见字符，只支持数字，大小写英文以及部分特殊字符：!#$&'()*+,/:;=?@-._~， 其它字符请自行编码为合法字符（因不支持%，中文无法使用 urlencode 处理，请使用其他编码方式）
     * @param page       – 必须是已经发布的小程序页面，例如 "pages/index/index" ,如果不填写这个字段，默认跳主页面
     * @param checkPath  – 默认true 检查 page 是否存在，为 true 时 page 必须是已经发布的小程序存在的页面（否则报错）； 为 false 时允许小程序未发布或者 page 不存在，但 page 有数量上限（60000个）请勿滥用
     * @param envVersion – 默认"release" 要打开的小程序版本。正式版为 "release"，体验版为 "trial"，开发版为 "develop"
     * @param width      – 默认430 二维码的宽度
     * @param isHyaline  – 是否需要透明底色， is_hyaline 为true时，生成透明底色的小程序码
     * @return String
     **/
    String getQrCode(String scene, String page, Boolean checkPath, String envVersion, Integer width, Boolean isHyaline);
}
