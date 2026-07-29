/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.support;

import java.util.Map;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

/**
 * Rocket发送调度器.
 *
 * @author zengdegui
 * @since 2026/05/08 16:36
 */
public interface RocketSenderDispatcher {

    /**
     * 发送
     *
     * @param cluster  集群名称
     * @param topic    主题名称
     * @param tag      标签
     * @param payload  消息体
     * @param headers  消息头
     * @param executor 执行器
     * @param <T>      泛型
     * @return T
     */
    <T> T dispatch(
        String cluster,
        String topic,
        String tag,
        Object payload,
        Map<String, Object> headers,
        RocketSendExecutor<T> executor);

    /**
     * 统一发送执行器
     */
    @FunctionalInterface
    interface RocketSendExecutor<T> {

        /**
         * 执行发送
         *
         * @param producer 生产者
         * @param message  消息
         * @return 发送结果
         * @throws Exception
         */
        T execute(DefaultMQProducer producer, Message message) throws Exception;
    }
}
