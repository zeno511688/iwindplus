/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support;

import com.iwindplus.base.kafka.domain.dto.KafkaMultiListenerMetaDTO;
import java.time.Duration;
import java.util.Map;

/**
 * kafka重试助手.
 *
 * @author zengdegui
 * @since 2026/05/24 13:34
 */
public interface KafkaRetryHandler {

    /**
     * 最大重试次数
     *
     * @param cluster 集群
     * @return int
     */
    int maxRetries(String cluster);

    /**
     * 判断是否可重试
     *
     * @param cluster    集群
     * @param retryCount 重试次数
     * @return boolean
     */
    boolean isRetryable(String cluster, int retryCount);

    /**
     * 下一次重试次数
     *
     * @param currentRetryCount 当前重试次数
     * @return int
     */
    int nextRetryCount(int currentRetryCount);

    /**
     * 获取重试topic
     *
     * @param cluster    集群
     * @param retryCount 重试次数
     * @return String
     */
    String retryTopic(String cluster, int retryCount);

    /**
     * 获取重试延迟
     *
     * @param retryCount 重试次数
     * @return Duration
     */
    Duration retryDelay(int retryCount);

    /**
     * 构建重试headers
     *
     * @param meta            监听器元数据
     * @param originTopic     原始主题
     * @param originPartition 原始分区
     * @param originOffset    原始偏移
     * @param retryCount      重试次数
     * @param firstFailTime   首次失败时间
     * @param error           异常
     * @return Map<String, byte [ ]>
     */
    Map<String, byte[]> buildRetryHeaders(
        KafkaMultiListenerMetaDTO meta,
        String originTopic,
        int originPartition,
        long originOffset,
        int retryCount,
        long firstFailTime,
        Throwable error
    );
}
