/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.wechat.support.impl;

import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyResult;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.wechat.domain.property.WechatProperty;
import com.iwindplus.base.wechat.support.WechatPayExecuteHandler;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

/**
 * 微信支付相关业务层接口实现类.
 *
 * @author zengdegui
 * @since 2020/11/29
 */
@Slf4j
public class WechatPayExecuteHandlerImpl extends WxPayServiceImpl implements WechatPayExecuteHandler {

    /**
     * 配置.
     */
    private WechatProperty.PayConfig config;

    /**
     * 构造函数.
     *
     * @param config 微信支付配置
     */
    public WechatPayExecuteHandlerImpl(WechatProperty.PayConfig config) {
        this.config = config;
        super.setConfig(config);
    }

    @Override
    public WechatProperty.PayConfig getConfig() {
        return this.config;
    }

    @Override
    public void setConfig(WechatProperty.PayConfig config) {
        this.config = config;
        super.setConfig(config);
    }

    @Override
    public boolean isHealthy() {
        return this.config != null && Boolean.TRUE.equals(this.config.getEnabled());
    }

    @Override
    public int getPriority() {
        return Optional.ofNullable(this.config)
            .map(WechatProperty.PayConfig::getPriority)
            .orElse(Integer.MAX_VALUE);
    }

    @Override
    public String getCode() {
        return this.config != null ? this.config.getCode() : null;
    }

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
