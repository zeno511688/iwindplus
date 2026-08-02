/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.handler;

import com.iwindplus.base.disruptor.support.DisruptorEventHandler;
import com.iwindplus.base.kafka.domain.dto.KafkaMultiListenerMetaDTO;
import com.iwindplus.base.kafka.domain.event.KafkaDisruptorEvent;
import com.iwindplus.base.kafka.support.KafkaListenerInvoker;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka Disruptor事件处理器. 业务执行成功后只通知消费线程，不在Disruptor线程操作Kafka Consumer或Acknowledgment.
 *
 * @author zengdegui
 * @since 2026/07/30
 */
@Slf4j
public record KafkaDisruptorEventHandler(
    KafkaListenerInvoker listenerInvoker) implements DisruptorEventHandler<KafkaDisruptorEvent> {

    @Override
    public void execute(
        KafkaDisruptorEvent data,
        long sequence,
        boolean endOfBatch) {
        KafkaMultiListenerMetaDTO meta = listenerInvoker.getMeta(data.getListenerId());
        if (meta == null) {
            log.warn("Kafka listener meta not found, listenerId={}", data.getListenerId());
            return;
        }

        listenerInvoker.invoke(meta, data.getMessages(), null, null);

        data.execute();
    }
}
