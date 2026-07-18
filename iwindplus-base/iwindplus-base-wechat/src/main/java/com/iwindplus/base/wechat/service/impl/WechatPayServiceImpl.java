/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.wechat.service.impl;

import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyResult;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.wechat.service.WechatPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

/**
 * 微信支付相关业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/11/29
 */
@Slf4j
public class WechatPayServiceImpl extends WxPayServiceImpl implements WechatPayService {
    @Override
    public WxPayOrderNotifyResult orderNotify(HttpServletRequest request) {
        try {
            String xmlResult = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
            return this.parseOrderNotifyResult(xmlResult);
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
        return null;
    }

    @Override
    public WxPayRefundNotifyResult refundNotify(HttpServletRequest request) {
        try {
            String xmlResult = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
            return this.parseRefundNotifyResult(xmlResult);
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
        return null;
    }
}
