/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.domain.dto;

import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Rabbit 消息数据传输对象.
 *
 * @author zengdegui
 * @since 2026/03/26 00:59
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RabbitMessageDTO implements Serializable {

    /**
     * 集群
     */
    private String cluster;

    /**
     * 交换机
     */
    private String exchange;

    /**
     * 路由key
     */
    private String routingKey;

    /**
     * 消息体
     */
    private Object message;

    /**
     * 消息头
     */
    private Map<String, Object> headers;
}
