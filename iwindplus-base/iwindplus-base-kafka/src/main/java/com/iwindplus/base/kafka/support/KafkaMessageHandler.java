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
     * 集群ID.
     */
    private String clusterId;

    /**
     * listenerId.
     */
    private String listenerId;

    /**
     * clientId.
     */
    private String clientId;

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
     * 消息列表.
     */
    private List<ConsumerRecord<String, Object>> messages;

    /**
     * 批量消息处理.
     */
    private final Consumer<List<ConsumerRecord<String, Object>>> batchHandler;

    /**
     * 成功回调.
     */
    private final Consumer<List<ConsumerRecord<String, Object>>> successCallbackHandler;

    public KafkaMessageHandler(
        String clusterId,
        String listenerId,
        String clientId,
        String cluster,
        String[] topics,
        String group,
        List<ConsumerRecord<String, Object>> messages,
        Consumer<List<ConsumerRecord<String, Object>>> batchHandler,
        Consumer<List<ConsumerRecord<String, Object>>> successCallbackHandler) {

        this.clusterId = clusterId;
        this.listenerId = listenerId;
        this.clientId = clientId;
        this.cluster = cluster;
        this.topics = topics;
        this.group = group;
        this.messages = messages;
        this.batchHandler = batchHandler;
        this.successCallbackHandler = successCallbackHandler;
    }

    /**
     * 处理批量消息.
     */
    public void executeMessage() {
        if (batchHandler != null) {
            batchHandler.accept(messages);
        }
    }

    /**
     * 处理批量消息和回调.
     */
    public void execute() {
        executeMessage();

        if (successCallbackHandler != null) {
            successCallbackHandler.accept(messages);
        }
    }
}
