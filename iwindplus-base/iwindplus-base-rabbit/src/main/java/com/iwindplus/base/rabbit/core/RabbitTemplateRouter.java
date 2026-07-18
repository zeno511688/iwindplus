/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.core;

import cn.hutool.core.util.IdUtil;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import com.iwindplus.base.rabbit.support.RabbitSenderDispatcher;
import com.iwindplus.base.rabbit.support.RabbitSenderDispatcher.RabbitSendExecutor;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;

/**
 * Rabbit模板路由器.
 *
 * @author zengdegui
 * @since 2026/03/20 21:56
 */
@Slf4j
public record RabbitTemplateRouter(
    RabbitClusterManager manager,
    RabbitSenderDispatcher dispatcher,
    TraceContextPropagator traceContextPropagator) {

    /**
     * 获取 AmqpAdmin
     *
     * @param cluster 集群名称
     * @return AmqpAdmin
     */
    public AmqpAdmin getAdmin(String cluster) {
        return manager.getAdmin(cluster);
    }

    /**
     * 默认集群发送消息
     *
     * @param exchange   交换机名称
     * @param routingKey 路由key
     * @param message    消息
     */
    public void send(String exchange, String routingKey, Object message) {
        this.send(manager.getDefaultCluster(), exchange, routingKey, message);
    }

    /**
     * 发送消息
     *
     * @param cluster    集群名称
     * @param exchange   交换机名称
     * @param routingKey 路由key
     * @param message    消息
     */
    public void send(String cluster, String exchange, String routingKey, Object message) {
        this.send(cluster, exchange, routingKey, message, null);
    }

    /**
     * 发送消息
     *
     * @param cluster    集群名称
     * @param exchange   交换机名称
     * @param routingKey 路由key
     * @param message    消息
     * @param headers    请求头
     */
    public void send(String cluster, String exchange, String routingKey,
        Object message, Map<String, Object> headers) {
        executeSend(cluster, exchange, routingKey, message, headers,
            (producer, msg) -> {
                producer.convertAndSend(
                    msg.getExchange(),
                    msg.getRoutingKey(),
                    msg.getMessage(),
                    buildPostProcessor(msg.getHeaders())
                );
                return null;
            }
        );
    }

    /**
     * 统一发送逻辑（带 Observation）
     */
    private <T> T executeSend(
        String cluster,
        String exchange,
        String routingKey,
        Object message,
        Map<String, Object> headers,
        RabbitSendExecutor<T> executor) {

        return dispatcher.dispatch(cluster, exchange, routingKey, message, headers, executor);
    }

    private MessagePostProcessor buildPostProcessor(
        Map<String, Object> headers) {

        return message -> {
            MessageProperties properties =
                message.getMessageProperties();

            properties.setMessageId(IdUtil.simpleUUID());

            traceContextPropagator.inject(
                message,
                RabbitSenderDispatcher.RABBIT_SETTER
            );

            if (headers != null) {
                headers.forEach(properties::setHeader);
            }

            return message;
        };
    }
}
