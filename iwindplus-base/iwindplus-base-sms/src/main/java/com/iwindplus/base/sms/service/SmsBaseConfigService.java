/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.service;

import com.iwindplus.base.sms.domain.property.SmsProperty;

/**
 * 短信业务层基础配置接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface SmsBaseConfigService {

    /**
     * 获取配置.
     *
     * @return SmsProperty
     */
    SmsProperty getConfig();

    /**
     * 设置配置.
     *
     * @param config 对象
     */
    void setConfig(SmsProperty config);
}
