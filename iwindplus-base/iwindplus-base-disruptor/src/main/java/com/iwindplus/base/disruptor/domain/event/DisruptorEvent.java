/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.disruptor.domain.event;

import com.iwindplus.base.domain.event.BaseEvent;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Disruptor事件.
 *
 * @author zengdegui
 * @since 2026/06/16 20:22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DisruptorEvent<T> extends BaseEvent<T> {

    /**
     * 消息头
     */
    private Map<String, String> headers;

    /**
     * 事件处理器名称（必填，对应DisruptorEventHandler.getName()）
     */
    private String handlerName;

    /**
     * 发布时间
     */
    private Long publishTime;

    /**
     * 数据来源. Kafka RabbitMQ HTTP Timer
     */
    private String source;

    /**
     * 数据去向. MySQL Redis ES Business
     */
    private String destination;

    /**
     * 重置
     */
    public void clear() {
        if (headers != null) {
            headers.clear();
        }
        handlerName = null;
        publishTime = null;
        source = null;
        destination = null;
        this.setData(null);
    }
}
