/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.kafka.core.KafkaClusterManager;
import com.iwindplus.base.kafka.domain.constant.KafkaConstant.BizRetryConstant;
import com.iwindplus.base.kafka.domain.constant.KafkaConstant.BizRetryHeaderConstant;
import com.iwindplus.base.kafka.domain.dto.KafkaMultiListenerMetaDTO;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty;
import com.iwindplus.base.kafka.support.KafkaRetryHandler;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * kafka重试助手实现类.
 *
 * @author zengdegui
 * @since 2026/05/24 13:36
 */
@Slf4j
public record DefaultKafkaRetryHandler(KafkaMultiProperty property, KafkaClusterManager clusterManager) implements KafkaRetryHandler {

    @Override
    public int maxRetries(String cluster) {
        return clusterManager.getConsumerConfig(cluster).getBizMaxRetryCount();
    }

    @Override
    public boolean isRetryable(String cluster, int retryCount) {
        return retryCount < this.maxRetries(cluster);
    }

    @Override
    public int nextRetryCount(int currentRetryCount) {
        return currentRetryCount + 1;
    }

    @Override
    public String retryTopic(String cluster, int retryCount) {
        return switch (retryCount) {
            case 1 -> BizRetryConstant.KAFKA_RETRY_5S;
            case 2 -> BizRetryConstant.KAFKA_RETRY_1M;
            case 3 -> BizRetryConstant.KAFKA_RETRY_5M;
            case 4 -> BizRetryConstant.KAFKA_RETRY_30M;
            case 5 -> BizRetryConstant.KAFKA_RETRY_1H;
            default -> BizRetryConstant.KAFKA_RETRY;
        };
    }

    @Override
    public Duration retryDelay(int retryCount) {
        return switch (retryCount) {
            case 1 -> BizRetryConstant.RETRY_5S;
            case 2 -> BizRetryConstant.RETRY_1M;
            case 3 -> BizRetryConstant.RETRY_5M;
            case 4 -> BizRetryConstant.RETRY_30M;
            case 5 -> BizRetryConstant.RETRY_1H;
            default -> Duration.ZERO;
        };
    }

    @Override
    public Map<String, byte[]> buildRetryHeaders(
        KafkaMultiListenerMetaDTO meta,
        String originTopic,
        int originPartition,
        long originOffset,
        int retryCount,
        long firstFailTime,
        Throwable error) {
        Map<String, byte[]> h = new HashMap<>(16);

        h.put(BizRetryHeaderConstant.APPLICATION_HEADER, SpringUtil.getApplicationName().getBytes(StandardCharsets.UTF_8));
        h.put(BizRetryHeaderConstant.ORIGIN_CLUSTER_HEADER, meta.getCluster().getBytes(StandardCharsets.UTF_8));
        h.put(BizRetryHeaderConstant.ORIGIN_GROUP_HEADER, meta.getGroup().getBytes(StandardCharsets.UTF_8));
        h.put(BizRetryHeaderConstant.ORIGIN_TOPIC_HEADER, originTopic.getBytes(StandardCharsets.UTF_8));
        h.put(BizRetryHeaderConstant.ORIGIN_PARTITION_HEADER, String.valueOf(originPartition).getBytes(StandardCharsets.UTF_8));
        h.put(BizRetryHeaderConstant.ORIGIN_OFFSET_HEADER, String.valueOf(originOffset).getBytes(StandardCharsets.UTF_8));
        h.put(BizRetryHeaderConstant.RETRY_TOPIC_HEADER, retryTopic(meta.getCluster(), retryCount).getBytes(StandardCharsets.UTF_8));
        h.put(BizRetryHeaderConstant.RETRY_COUNT_HEADER, String.valueOf(retryCount).getBytes(StandardCharsets.UTF_8));
        h.put(BizRetryHeaderConstant.FIRST_FAIL_TIME_HEADER, String.valueOf(firstFailTime).getBytes(StandardCharsets.UTF_8));

        if (error != null) {
            h.put(BizRetryHeaderConstant.ERROR_CLASS_HEADER, error.getClass().getName().getBytes(StandardCharsets.UTF_8));
            h.put(BizRetryHeaderConstant.ERROR_MESSAGE_HEADER, error.getMessage().getBytes(StandardCharsets.UTF_8));
        }

        return h;
    }
}
