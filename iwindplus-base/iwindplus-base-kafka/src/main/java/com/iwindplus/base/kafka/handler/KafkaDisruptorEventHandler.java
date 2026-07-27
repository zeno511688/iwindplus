/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.handler;

import com.iwindplus.base.disruptor.support.DisruptorEventHandler;
import com.iwindplus.base.kafka.support.KafkaMessageHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka Disruptor事件处理器.
 *
 * @author zengdegui
 * @since 2026/07/26 15:15
 */
@Slf4j
public class KafkaDisruptorEventHandler implements DisruptorEventHandler<KafkaMessageHandler> {

    @Override
    public void execute(
        KafkaMessageHandler data,
        long sequence,
        boolean endOfBatch) {

        data.execute();
    }
}
