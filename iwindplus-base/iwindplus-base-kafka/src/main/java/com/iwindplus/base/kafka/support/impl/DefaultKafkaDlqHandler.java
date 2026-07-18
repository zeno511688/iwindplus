/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.kafka.core.KafkaClusterManager;
import com.iwindplus.base.kafka.domain.dto.KafkaMultiListenerMetaDTO;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty;
import com.iwindplus.base.kafka.support.KafkaDlqHandler;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * kafka死信助手实现类.
 *
 * @author zengdegui
 * @since 2026/05/24 13:38
 */
@Slf4j
public record DefaultKafkaDlqHandler(KafkaMultiProperty property, KafkaClusterManager clusterManager) implements KafkaDlqHandler {

    @Override
    public String dlqTopic(String cluster, String topic) {
        return clusterManager.getConsumerConfig(cluster).getDlqTopic();
    }

    @Override
    public Map<String, Object> buildDlqPayload(
        KafkaMultiListenerMetaDTO meta,
        String originTopic,
        int originPartition,
        long originOffset,
        Object value,
        int retryCount,
        long firstFailTime,
        Throwable error) {
        Map<String, Object> map = new HashMap<>(16);

        map.put("application", SpringUtil.getApplicationName());
        map.put("cluster", meta.getCluster());
        map.put("group", meta.getGroup());
        map.put("originTopic", originTopic);
        map.put("originPartition", originPartition);
        map.put("originOffset", originOffset);
        map.put("retryCount", retryCount);
        map.put("firstFailTime", firstFailTime);
        map.put("value", value);
        map.put("dlq_source", retryCount > 0 ? "RETRY" : "NORMAL");

        map.put("error", error == null ? null : error.getClass().getName());
        map.put("message", error == null ? null : error.getMessage());

        return map;
    }
}
