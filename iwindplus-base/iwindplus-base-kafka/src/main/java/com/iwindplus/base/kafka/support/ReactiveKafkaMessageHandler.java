/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support;

import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

/**
 * Reactive Kafka接收调度器.
 *
 * @author zengdegui
 * @since 2026/05/12 00:31
 */
@Slf4j
@Getter
public class ReactiveKafkaMessageHandler {

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
    private final Function<List<ReceiverRecord<String, Object>>, Mono<Void>> batchHandler;

    public ReactiveKafkaMessageHandler(
        String cluster,
        String[] topics,
        String group,
        Function<List<ReceiverRecord<String, Object>>, Mono<Void>> handler) {
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
    public Mono<Void> handleBatch(List<ReceiverRecord<String, Object>> msgs) {
        if (batchHandler == null) {
            return Mono.empty();
        }

        return batchHandler.apply(msgs);
    }
}
