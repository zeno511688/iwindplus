/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.disruptor.domain.dto;

import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Disruptor发布数据对象.
 *
 * @param <T>泛型
 * @author zengdegui
 * @since 2026/07/27 16:08
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DisruptorPublishDTO<T> implements Serializable {

    /**
     * 事件处理器名称（必填，对应DisruptorEventHandler.getName()）
     */
    private String handlerName;

    /**
     * 发送数据（必填）
     */
    private T data;

    /**
     * 消息头（可选）
     */
    private Map<String, String> headers;

    /**
     * 发送方来源（可选，需要监控时用）
     */
    private String source;

    /**
     * 发送目的地（可选，需要监控时用）
     */
    private String destination;
}
