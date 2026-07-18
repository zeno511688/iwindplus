/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.wechat.service;

import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyResult;
import com.github.binarywang.wxpay.service.WxPayService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 微信支付相关业务层接口.
 *
 * @author zengdegui
 * @since 2020/11/29
 */
public interface WechatPayService extends WxPayService {
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
