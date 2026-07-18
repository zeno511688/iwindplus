/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;
import java.time.Duration;
import java.util.List;

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
     * Retry Topics
     */
    public static final List<String> KAFKA_RETRY_SUFFIXES = List.of(
        BizRetryConstant.KAFKA_RETRY,
        BizRetryConstant.KAFKA_RETRY_5S,
        BizRetryConstant.KAFKA_RETRY_1M,
        BizRetryConstant.KAFKA_RETRY_5M,
        BizRetryConstant.KAFKA_RETRY_30M,
        BizRetryConstant.KAFKA_RETRY_1H
    );

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
     * 监控指标名称 .
     */
    public final class MetricName {

        private MetricName() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 重试发送成功
         */
        public static final String RETRY_SEND_SUCCESS = "kafka.retry.send.success";

        /**
         * 重试发送失败
         */
        public static final String RETRY_SEND_FAILURE = "kafka.retry.send.failure";

        /**
         * DLQ发送成功
         */
        public static final String DLQ_SEND_SUCCESS = "kafka.dlq.send.success";

        /**
         * DLQ发送失败
         */
        public static final String DLQ_SEND_FAILURE = "kafka.dlq.send.failure";

        /**
         * 重试发送耗时
         */
        public static final String RETRY_SEND_DURATION = "kafka.retry.send.duration";

        /**
         * DLQ发送耗时
         */
        public static final String DLQ_SEND_DURATION = "kafka.dlq.send.duration";

        /**
         * 消费处理耗时
         */
        public static final String CONSUME_PROCESS_DURATION = "kafka.consume.process.duration";

        /**
         * 消费失败次数
         */
        public static final String CONSUME_FAILURE = "kafka.consume.failure";
    }

    /**
     * 业务重试头相关常数 .
     */
    public final class BizRetryHeaderConstant {

        private BizRetryHeaderConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 应用
         */
        public static final String APPLICATION_HEADER = "application";

        /**
         * 原始集群
         */
        public static final String ORIGIN_CLUSTER_HEADER = "x-origin-cluster";

        /**
         * 原始消费者
         */
        public static final String ORIGIN_GROUP_HEADER = "x-origin-group";

        /**
         * 原始主题
         */
        public static final String ORIGIN_TOPIC_HEADER = "x-origin-topic";

        /**
         * 原始分区
         */
        public static final String ORIGIN_PARTITION_HEADER = "x-origin-partition";

        /**
         * 原始偏移量
         */
        public static final String ORIGIN_OFFSET_HEADER = "x-origin-offset";

        /**
         * 重试主题
         */
        public static final String RETRY_TOPIC_HEADER = "x-retry-topic";

        /**
         * 重试次数
         */
        public static final String RETRY_COUNT_HEADER = "x-retry-count";

        /**
         * 首次失败时间
         */
        public static final String FIRST_FAIL_TIME_HEADER = "x-first-fail-time";

        /**
         * 错误类
         */
        public static final String ERROR_CLASS_HEADER = "x-error-class";

        /**
         * 错误消息
         */
        public static final String ERROR_MESSAGE_HEADER = "x-message-class";
    }

    /**
     * 业务重试相关常数 .
     */
    public final class BizRetryConstant {

        private BizRetryConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * retry
         */
        public static final String KAFKA_RETRY = "kafka_retry";

        /**
         * dead letter queue
         */
        public static final String KAFKA_DLQ = "kafka_dlq";

        /**
         * retry 5s
         */
        public static final String KAFKA_RETRY_5S = "kafka_retry_5s";

        /**
         * retry 1m
         */
        public static final String KAFKA_RETRY_1M = "kafka_retry_1m";

        /**
         * retry 5m
         */
        public static final String KAFKA_RETRY_5M = "kafka_retry_5m";

        /**
         * retry 30m
         */
        public static final String KAFKA_RETRY_30M = "kafka_retry_30m";

        /**
         * retry 1h
         */
        public static final String KAFKA_RETRY_1H = "kafka_retry_1h";

        /**
         * retry 5s
         */
        public static final Duration RETRY_5S = Duration.ofSeconds(5);

        /**
         * retry 1m
         */
        public static final Duration RETRY_1M = Duration.ofMinutes(1);

        /**
         * retry 5m
         */
        public static final Duration RETRY_5M = Duration.ofMinutes(5);

        /**
         * retry 30m
         */
        public static final Duration RETRY_30M = Duration.ofMinutes(30);

        /**
         * retry 1h
         */
        public static final Duration RETRY_1H = Duration.ofHours(1);
    }

}
