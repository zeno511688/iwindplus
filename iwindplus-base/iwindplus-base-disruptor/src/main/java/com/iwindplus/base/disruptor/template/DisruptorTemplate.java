/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.disruptor.template;

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
     * @param source      数据来源
     * @param destination 数据去向
     * @param data        数据
     */
    void publish(String source, String destination, T data);
}
