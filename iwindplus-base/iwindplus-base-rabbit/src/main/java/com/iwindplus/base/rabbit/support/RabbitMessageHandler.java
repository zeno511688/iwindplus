/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.support;

import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;

/**
 * rabbit 消息处理助手.
 *
 * @author zengdegui
 * @since 2026/04/07 22:32
 */
@Slf4j
@Getter
public class RabbitMessageHandler {

    /**
     * 集群.
     */
    private String cluster;

    /**
     * 监听器队列
     */
    private String[] queues;

    /**
     * 消费组
     */
    private String group;

    /**
     * 消息列表.
     */
    private List<Message> messages;

    /**
     * 批量消息处理.
     */
    private final Consumer<List<Message>> batchHandler;

    public RabbitMessageHandler(
        String cluster,
        String[] queues,
        String group,
        List<Message> messages,
        Consumer<List<Message>> batchHandler) {
        this.cluster = cluster;
        this.queues = queues;
        this.group = group;
        this.messages = messages;
        this.batchHandler = batchHandler;
    }

    /**
     * 处理批量消息.
     *
     * @param messages 消息列表
     */
    public void handleBatch(List<Message> messages) {
        if (batchHandler != null) {
            batchHandler.accept(messages);
        }
    }
}
