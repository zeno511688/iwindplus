/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support;

import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * kafka 消息处理助手.
 *
 * @author zengdegui
 * @since 2026/04/07 22:32
 */
@Slf4j
@Getter
public class KafkaMessageHandler {

    /**
     * 集群.
     */
    private String cluster;

    /**
     * 主题名称
     */
    private String[] topics;

    /**
     * 消费组
     */
    private String group;

    /**
     * 批量消息处理.
     */
    private final Consumer<List<ConsumerRecord<String, Object>>> batchHandler;

    public KafkaMessageHandler(
        String cluster,
        String[] topics,
        String group,
        Consumer<List<ConsumerRecord<String, Object>>> handler) {
        this.cluster = cluster;
        this.topics = topics;
        this.group = group;
        this.batchHandler = handler;
    }

    /**
     * 处理批量消息.
     *
     * @param msgs 消息列表
     */
    public void handleBatch(List<ConsumerRecord<String, Object>> msgs) {
        if (batchHandler != null) {
            batchHandler.accept(msgs);
        }
    }
}
