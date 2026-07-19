/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2018/12/27
 */
public final class KafkaConstant {

    private KafkaConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * kafka.
     */
    public static final String KAFKA = "kafka";

    /**
     * 生产者.
     */
    public static final String KAFKA_PRODUCER = "kafka.producer";

    /**
     * 消费者.
     */
    public static final String KAFKA_CONSUMER = "kafka.consumer";

    /**
     * 生产者后缀.
     */
    public static final String PRODUCER_SUFFIX = "producer";

    /**
     * 消费者后缀.
     */
    public static final String CONSUMER_SUFFIX = "consumer";

    /**
     * 默认的消费者组.
     */
    public static final String KAFKA_DEFAULT_GROUP = "default-group";

    /**
     * 集群
     */
    public static final String CLUSTER = "cluster";

    /**
     * 主题
     */
    public static final String TOPIC = "topic";

    /**
     * key
     */
    public static final String KEY = "key";

    /**
     * 消费组
     */
    public static final String GROUP = "group";

    /**
     * 预取数量
     */
    public static final int PREFETCH = 1;

    /**
     * 重试topic后缀
     */
    public static final String KAFKA_RETRY_SUFFIX = ".RETRY";

    /**
     * 重试topic消费组后缀
     */
    public static final String KAFKA_RETRY_GROUP_SUFFIX = "-retry";

    /**
     * 死信队列后缀
     */
    public static final String KAFKA_DLQ_SUFFIX = ".DLQ";

    /**
     * 死信队列消费组后缀
     */
    public static final String KAFKA_DLQ_GROUP_SUFFIX = "-dlq";
}
