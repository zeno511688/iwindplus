/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.wechat.support;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.iwindplus.base.wechat.domain.dto.WechatMaQrCodeDTO;
import com.iwindplus.base.wechat.domain.dto.WechatMaUserInfoDTO;
import com.iwindplus.base.wechat.domain.property.WechatProperty.MaConfig;
import com.iwindplus.base.wechat.domain.vo.WechatMaPhoneNumberVO;
import com.iwindplus.base.wechat.domain.vo.WechatMaUserInfoVO;
import com.iwindplus.base.wechat.service.BaseConfigService;
import com.iwindplus.base.wechat.service.BaseService;

/**
 * 微信小程序策略标识接口.
 *
 * <p>每个小程序配置对应一个运行时策略实例，通过配置编码选择具体实例。</p>
 *
 * @author zengdegui
 * @since 2026/9/1
 */
public interface WechatMaExecuteHandler extends WxMaService, BaseConfigService<MaConfig>, BaseService {

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
     * @param entity 对象
     * @return WechatMaUserInfoVO
     */
    WechatMaUserInfoVO getUserInfo(WechatMaUserInfoDTO entity);

    /**
     * 获取小程序二维码.
     *
     * @param entity 对象
     * @return String
     **/
    String getQrCode(WechatMaQrCodeDTO entity);
}
