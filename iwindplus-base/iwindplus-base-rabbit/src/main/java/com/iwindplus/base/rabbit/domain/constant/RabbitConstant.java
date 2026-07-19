/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * rabbit常数.
 *
 * @author zengdegui
 * @since 2024/06/10 20:03
 */
public final class RabbitConstant {

    private RabbitConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * rabbit.
     */
    public static final String RABBIT = "rabbit";

    /**
     * 生产者.
     */
    public static final String RABBIT_PRODUCER = "rabbitmq.producer";

    /**
     * 消费者.
     */
    public static final String RABBIT_CONSUMER = "rabbitmq.consumer";

    /**
     * 默认的消费者组.
     */
    public static final String RABBIT_DEFAULT_GROUP = "default-group";

    /**
     * 消息存活时间.
     */
    public static final String X_MESSAGE_TTL = "x-message-ttl";

    /**
     * 队列的优先级.
     */
    public static final String X_MAX_PRIORITY = "x-max-priority";

    /**
     * 延时消息.
     */
    public static final String X_DELAYED_MESSAGE = "x-delayed-message";

    /**
     * 延时消息交换机类型.
     */
    public static final String X_DELAYED_TYPE = "x-delayed-type";

    /**
     * 绑定死信队列的交换机名称.
     */
    public static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";

    /**
     * 绑定死信队列的路由key.
     */
    public static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

    /**
     * 集群
     */
    public static final String CLUSTER = "cluster";

    /**
     * 交换机
     */
    public static final String EXCHANGE = "exchange";

    /**
     * 路由key
     */
    public static final String ROUTING_KEY = "routingKey";

    /**
     * 队列
     */
    public static final String QUEUES = "queues";

    /**
     * 消费组
     */
    public static final String GROUP = "group";
}
