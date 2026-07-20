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
     * 死信队列后缀
     */
    public static final String KAFKA_DLQ_SUFFIX = ".DLQ";

    /**
     * 死信队列消费组后缀
     */
    public static final String KAFKA_DLQ_GROUP_SUFFIX = "-group-dlq";

    /**
     * 重试请求头 .
     */
    public final class RetryHeadersConstant {

        private RetryHeadersConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 头前缀 .
         */
        public static final String PREFIX = "kafka-retry-";

        /**
         * 原主题
         */
        public static final String ORIGINAL_TOPIC = PREFIX + "original-topic";

        /**
         * 原partition
         */
        public static final String ORIGINAL_PARTITION = PREFIX + "original-partition";

        /**
         * 原offset
         */
        public static final String ORIGINAL_OFFSET = PREFIX + "original-offset";

        /**
         * 重试次数
         */
        public static final String RETRY_COUNT = PREFIX + "retry-count";

        /**
         * 最大重试次数
         */
        public static final String MAX_RETRY = PREFIX + "max-retry";

        /**
         * 错误类型
         */
        public static final String ERROR_TYPE = PREFIX + "error-type";

        /**
         * 错误信息
         */
        public static final String ERROR_MESSAGE = PREFIX + "error-message";

        /**
         * 首次错误发生时间
         */
        public static final String FIRST_FAILED_TIME = PREFIX + "first-failed-time";

        /**
         * 最后错误发生时间
         */
        public static final String LAST_FAILED_TIME = PREFIX + "last-failed-time";
    }
}
