/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support;

import com.iwindplus.base.kafka.domain.dto.KafkaMultiListenerMetaDTO;
import java.util.Map;

/**
 * kafka 死信队列助手.
 *
 * @author zengdegui
 * @since 2026/05/24 13:36
 */
public interface KafkaDlqHandler {

    /**
     * 获取死信队列Topic.
     *
     * @param cluster kafka集群
     * @param topic   主题
     * @return String
     */
    String dlqTopic(String cluster, String topic);

    /**
     * 构建死信队列消息.
     *
     * @param meta            监听器元数据
     * @param originTopic     原始主题
     * @param originPartition 原始分区
     * @param originOffset    原始偏移量
     * @param value           数据
     * @param retryCount      重试次数
     * @param firstFailTime   首次失败时间
     * @param error           错误信息
     * @return Map<String, Object>
     */
    Map<String, Object> buildDlqPayload(
        KafkaMultiListenerMetaDTO meta,
        String originTopic,
        int originPartition,
        long originOffset,
        Object value,
        int retryCount,
        long firstFailTime,
        Throwable error
    );
}
