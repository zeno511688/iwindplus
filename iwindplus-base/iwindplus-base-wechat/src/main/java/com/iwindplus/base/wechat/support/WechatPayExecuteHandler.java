/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.wechat.support;

import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyResult;
import com.github.binarywang.wxpay.service.WxPayService;
import com.iwindplus.base.wechat.domain.property.WechatProperty.PayConfig;
import com.iwindplus.base.wechat.service.BaseConfigService;
import com.iwindplus.base.wechat.service.BaseService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 微信支付策略标识接口.
 *
 * <p>每个微信支付配置对应一个运行时策略实例，通过配置编码选择具体实例。</p>
 *
 * @author zengdegui
 * @since 2026/9/1
 */
public interface WechatPayExecuteHandler extends WxPayService, BaseConfigService<PayConfig>, BaseService {

    /**
     * 支付成功回调.
     *
     * @param request 请求
     * @return WxPayOrderNotifyResult
     */
    WxPayOrderNotifyResult orderNotify(HttpServletRequest request);

    /**
     * 退款成功回调.
     *
     * @param request 请求
     * @return WxPayRefundNotifyResult
     */
    WxPayRefundNotifyResult refundNotify(HttpServletRequest request);
}
