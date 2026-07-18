/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.service.impl;

import com.iwindplus.base.sms.domain.property.SmsProperty;
import com.iwindplus.base.sms.service.SmsBaseConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 短信业务基础配置抽象类.
 *
 * @author zengdegui
 * @since 2020/3/13
 */
@Slf4j
public abstract class AbstractSmsBaseConfigServiceImpl implements SmsBaseConfigService {

    @Resource
    private SmsProperty property;

    @Override
    public SmsProperty getConfig() {
        return this.property;
    }

    @Override
    public void setConfig(SmsProperty config) {
        this.property = config;
    }
}
