/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.support;

import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.rabbit.core.RabbitClusterManager;
import com.iwindplus.base.rabbit.domain.dto.RabbitMessageDTO;
import com.iwindplus.base.rabbit.support.observation.ClusterRabbitSenderObservationConvention;
import com.iwindplus.base.rabbit.support.observation.RabbitSenderObservationContext;
import io.micrometer.tracing.propagation.Propagator;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Rabbit发送调度器.
 *
 * @author zengdegui
 * @since 2026/05/08 16:36
 */
@Slf4j
public record RabbitSenderDispatcher(
    RabbitClusterManager manager,
    ObservationExecutor observationExecutor) {

    private static final ClusterRabbitSenderObservationConvention CONVENTION =
        new ClusterRabbitSenderObservationConvention();

    public static final Propagator.Setter<Message> RABBIT_SETTER =
        (message, key, value) ->
            message.getMessageProperties()
                .setHeader(key, value);

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
    public <T> T dispatch(
        String cluster,
        String exchange,
        String routingKey,
        Object message,
        Map<String, Object> headers,
        RabbitSendExecutor<T> executor) {

        validate(cluster, exchange, routingKey, message);
        RabbitTemplate template = manager.getTemplate(cluster);
        RabbitMessageDTO msg = RabbitMessageDTO.builder()
            .cluster(cluster)
            .exchange(exchange)
            .routingKey(routingKey)
            .message(message)
            .headers(headers)
            .build();

        final Boolean enabledObservation = manager.getProperty().getProducerEnabledObservation(cluster);
        if (Boolean.FALSE.equals(enabledObservation)) {
            return doExecute(template, executor, msg);
        }

        RabbitSenderObservationContext context =
            new RabbitSenderObservationContext(cluster, exchange, routingKey);

        return observationExecutor.execute(
            CONVENTION,
            () -> context,
            () -> doExecute(template, executor, msg)
        );
    }

    private void validate(String cluster, String exchange, String routingKey, Object message) {
        Objects.requireNonNull(cluster, "cluster must not be null");
        Objects.requireNonNull(exchange, "exchange must not be null");
        Objects.requireNonNull(routingKey, "routingKey must not be null");
        Objects.requireNonNull(message, "message must not be null");
    }

    private <T> T doExecute(
        RabbitTemplate template,
        RabbitSendExecutor<T> executor,
        RabbitMessageDTO msg) {
        try {
            final T result = executor.execute(template, msg);
            log.info("Rabbit send success, cluster={}, exchange={}, routingKey={}, headers={}",
                msg.getCluster(), msg.getExchange(), msg.getRoutingKey(), msg.getHeaders()
            );
            return result;
        } catch (Exception e) {
            throw wrapException(msg, e);
        }
    }

    private RuntimeException wrapException(RabbitMessageDTO msg, Exception e) {
        log.error("Rabbit send failed, cluster={}, exchange={}, routingKey={}, headers={}",
            msg.getCluster(), msg.getExchange(), msg.getRoutingKey(), msg.getHeaders(), e
        );
        return new RuntimeException(e);
    }

    /**
     * 统一发送执行器
     */
    @FunctionalInterface
    public interface RabbitSendExecutor<T> {

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
}
