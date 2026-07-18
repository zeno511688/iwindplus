/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mail.service.impl;

import com.iwindplus.base.mail.domain.property.MailProperty;
import com.iwindplus.base.mail.service.MailBaseConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 邮箱业务层基础配置抽象类.
 *
 * @author zengdegui
 * @since 2020/3/13
 */
@Slf4j
public abstract class AbstractMailBaseConfigServiceImpl implements MailBaseConfigService {
    @Resource
    private MailProperty property;

    @Override
    public MailProperty getConfig() {
        return this.property;
    }

    @Override
    public void setConfig(MailProperty config) {
        this.property = config;
    }
}
