/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.domain.event;

import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Kafka Disruptor事件，只传递业务数据和监听器唯一标识.
 *
 * @author zengdegui
 * @since 2026/07/30
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaDisruptorEvent {

    /**
     * 监听器唯一标识.
     */
    private String listenerId;

    /**
     * 消息列表.
     */
    private List<ConsumerRecord<String, Object>> messages;

    /**
     * 成功回调.
     */
    private Consumer<List<ConsumerRecord<String, Object>>> successCallbackHandler;

    /**
     * 回调.
     */
    public void execute() {
        if (successCallbackHandler != null) {
            successCallbackHandler.accept(messages);
        }
    }
}
