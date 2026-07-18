/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.support;

import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * rocket 消息处理助手.
 *
 * @author zengdegui
 * @since 2026/04/07 22:32
 */
@Slf4j
@Getter
public class RocketMessageHandler {

    /**
     * 集群.
     */
    private String cluster;

    /**
     * 主题名称.
     */
    private final String topic;

    /**
     * 消费组.
     */
    private final String group;

    /**
     * Tag（支持 * 或 多个用 || 分隔）
     */
    private final String tag;

    /**
     * 是否顺序消费.
     */
    private final boolean orderly;

    /**
     * 批量消息处理.
     */
    private final Consumer<List<MessageExt>> batchHandler;

    public RocketMessageHandler(
        String cluster,
        String topic,
        String tag,
        String group,
        boolean orderly,
        Consumer<List<MessageExt>> handler) {
        this.cluster = cluster;
        this.topic = topic;
        this.tag = tag;
        this.group = group;
        this.orderly = orderly;
        this.batchHandler = handler;
    }

    /**
     * 处理批量消息.
     *
     * @param msgs 消息列表
     */
    public void handleBatch(List<MessageExt> msgs) {
        if (batchHandler != null) {
            batchHandler.accept(msgs);
        }
    }
}
