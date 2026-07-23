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
     * 生产者后缀.
     */
    public static final String PRODUCER_SUFFIX = "-producer";

    /**
     * 消费者后缀.
     */
    public static final String CONSUMER_SUFFIX = "-consumer";

    /**
     * 集群
     */
    public static final String CLUSTER = "cluster";

    /**
     * 死信主题
     */
    public static final String KAFKA_DLT_SUFFIX = ".DLT";

    /**
     * 死信主题消费组后缀
     */
    public static final String KAFKA_DLT_GROUP_SUFFIX = "-dlt";
}
