/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.disruptor.template;

import com.iwindplus.base.disruptor.domain.dto.DisruptorPublishDTO;

/**
 * Disruptor模板.
 *
 * @author zengdegui
 * @since 2026/06/18 08:05
 */
public interface DisruptorTemplate<T> {

    /**
     * 发送事件.
     *
     * @param entity 对象
     * @return boolean
     */
    boolean publish(DisruptorPublishDTO<T> entity);
}
