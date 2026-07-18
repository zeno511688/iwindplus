/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mail.service;

import com.iwindplus.base.mail.domain.property.MailProperty;

/**
 * 邮箱业务层基础配置接口类.
 *
 * @author zengdegui
 * @since 2020/4/28
 */
public interface MailBaseConfigService {
    /**
     * 获取配置.
     *
     * @return MailProperty
     */
    MailProperty getConfig();

    /**
     * 设置配置.
     *
     * @param config 对象
     */
    void setConfig(MailProperty config);
}
