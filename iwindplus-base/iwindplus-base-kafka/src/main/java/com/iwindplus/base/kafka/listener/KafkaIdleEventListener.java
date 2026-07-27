/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.listener;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.event.ListenerContainerIdleEvent;

/**
 * kafka 空闲事件监听.
 *
 * @author zengdegui
 * @since 2026/07/27 10:49
 */
@Slf4j
public record KafkaIdleEventListener(
    KafkaMultiListenerRegistrar registrar) {

    /**
     * kafka 空闲事件监听.
     *
     * @param event kafka 空闲事件
     */
    @EventListener
    public void onIdle(ListenerContainerIdleEvent event) {
        String listenerId = StrUtil.subBefore(event.getListenerId(), "-", true);
        Consumer<?, ?> consumer = event.getConsumer();

        registrar.tryCommit(
            listenerId,
            consumer
        );
    }
}
