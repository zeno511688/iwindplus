/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.core;

import com.iwindplus.base.rocket.support.RocketSenderDispatcher;
import com.iwindplus.base.rocket.support.RocketSenderDispatcher.RocketSendExecutor;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;

/**
 * Rocket模板路由器.
 *
 * @author zengdegui
 * @since 2026/03/20 21:56
 */
@Slf4j
public record RocketTemplateRouter(RocketClusterManager manager, RocketSenderDispatcher dispatcher) {

    /**
     * 默认集群发送消息
     *
     * @param topic   topic 名称
     * @param tag     tag（可选）
     * @param message 消息体
     * @return SendResult
     */
    public SendResult send(String topic, String tag, Object message) {
        return this.send(manager.getDefaultCluster(), topic, tag, message, null);
    }

    /**
     * 指定集群发送消息
     *
     * @param cluster 集群名称
     * @param topic   topic 名称
     * @param tag     tag（可选）
     * @param message 消息体
     * @return SendResult
     */
    public SendResult send(String cluster, String topic, String tag, Object message) {
        return this.send(cluster, topic, tag, message, null);
    }

    /**
     * 指定集群发送消息（支持 headers）
     *
     * @param cluster 集群名称
     * @param topic   topic 名称
     * @param tag     tag（可选）
     * @param message 消息体
     * @param headers 扩展头
     * @return SendResult
     */
    public SendResult send(String cluster, String topic, String tag,
        Object message, Map<String, Object> headers) {

        return this.executeSend(cluster, topic, tag, message, headers,
            DefaultMQProducer::send
        );
    }

    /**
     * 异步发送
     *
     * @param cluster  集群
     * @param topic    topic
     * @param tag      tag
     * @param message  消息体
     * @param headers  扩展头
     * @param callback 回调（成功/失败）
     */
    public void send(String cluster, String topic, String tag,
        Object message, Map<String, Object> headers, SendCallback callback) {
        executeSend(cluster, topic, tag, message, headers,
            (producer, msg) -> {
                producer.send(msg, callback);
                return null;
            }
        );
    }

    /**
     * 统一发送逻辑（带 Observation）
     */
    private <T> T executeSend(
        String cluster,
        String topic,
        String tag,
        Object payload,
        Map<String, Object> headers,
        RocketSendExecutor<T> executor) {

        return dispatcher.dispatch(cluster, topic, tag, payload, headers, executor);
    }

}
