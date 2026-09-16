/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mail.service.impl;

import com.iwindplus.base.mail.domain.property.MailProperty;
import com.iwindplus.base.mail.service.BaseService;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * 邮件业务基础抽象类.
 *
 * @param <T> 配置类型
 * @author zengdegui
 * @since 2020/3/13
 */
@Slf4j
public abstract class AbstractBaseServiceImpl<T extends MailProperty.BaseConfig>
    extends AbstractBaseConfigServiceImpl<T> implements BaseService {

    /**
     * 健康检查.
     *
     * @return 是否健康
     */
    @Override
    public boolean isHealthy() {
        return this.config != null && Boolean.TRUE.equals(this.config.getEnabled());
    }

    /**
     * 获取优先级.
     *
     * @return 优先级（数字越小优先级越高）
     */
    @Override
    public int getPriority() {
        return Optional.ofNullable(this.getConfig().getPriority()).orElse(Integer.MAX_VALUE);
    }

    /**
     * 获取配置编码.
     *
     * @return 配置编码
     */
    @Override
    public String getCode() {
        return this.getConfig().getCode();
    }
}
