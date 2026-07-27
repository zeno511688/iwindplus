/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.disruptor.core;

import com.iwindplus.base.disruptor.domain.property.DisruptorMultiProperty;
import com.iwindplus.base.disruptor.template.DisruptorTemplate;

/**
 * Disruptor管理器 .
 *
 * @author zengdegui
 * @since 2025/09/21 20:18
 */
public interface DisruptorManager<T> {

    /**
     * 获取配置.
     *
     * @return DisruptorMultiProperty
     */
    DisruptorMultiProperty getProperty();

    /**
     * 获取DisruptorTemplate.
     *
     * @param name 名称
     * @return DisruptorTemplate
     */
    DisruptorTemplate<T> getTemplate(String name);
}
