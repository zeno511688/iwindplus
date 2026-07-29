/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.support;

import com.iwindplus.base.rabbit.domain.dto.RabbitMessageDTO;
import io.micrometer.tracing.propagation.Propagator;
import java.util.Map;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Rabbit发送调度器.
 *
 * @author zengdegui
 * @since 2026/05/08 16:36
 */
public interface RabbitSenderDispatcher {

    /**
     * 发送
     *
     * @param cluster    集群名称
     * @param exchange   交换机
     * @param routingKey 路由key
     * @param message    消息体
     * @param headers    消息头
     * @param executor   执行器
     * @param <T>        泛型
     * @return T
     */
    <T> T dispatch(
        String cluster,
        String exchange,
        String routingKey,
        Object message,
        Map<String, Object> headers,
        RabbitSendExecutor<T> executor);

    /**
     * 统一发送执行器
     */
    @FunctionalInterface
    interface RabbitSendExecutor<T> {

        /**
         * 执行发送
         *
         * @param producer 生产者
         * @param message  消息
         * @return 发送结果
         * @throws Exception
         */
        T execute(RabbitTemplate producer, RabbitMessageDTO message) throws Exception;
    }

    /**
     * Rabbit发送消息头设置器
     */
    Propagator.Setter<Message> RABBIT_SETTER = (message, key, value) ->
        message.getMessageProperties()
            .setHeader(key, value);
}
