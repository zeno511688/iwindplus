/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.support;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import com.iwindplus.base.rocket.core.RocketClusterManager;
import com.iwindplus.base.rocket.support.observation.ClusterRocketSenderObservationConvention;
import com.iwindplus.base.rocket.support.observation.RocketSenderObservationContext;
import com.iwindplus.base.util.JacksonUtil;
import io.micrometer.tracing.propagation.Propagator;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

/**
 * Rocket发送调度器.
 *
 * @author zengdegui
 * @since 2026/05/08 16:36
 */
@Slf4j
public record RocketSenderDispatcher(
    RocketClusterManager manager,
    TraceContextPropagator traceContextPropagator,
    ObservationExecutor observationExecutor) {

    private static final ClusterRocketSenderObservationConvention CONVENTION =
        new ClusterRocketSenderObservationConvention();

    public static final Propagator.Setter<Message> ROCKET_SETTER =
        (message, key, value) -> message.putUserProperty(key, value);

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
    public <T> T dispatch(
        String cluster,
        String topic,
        String tag,
        Object payload,
        Map<String, Object> headers,
        RocketSendExecutor<T> executor) {

        validate(cluster, topic, payload);
        DefaultMQProducer producer = manager.getProducer(cluster);
        Message msg = buildMessage(topic, tag, payload, headers);

        final Boolean enabledObservation = manager.getProperty().getProducerEnabledObservation(cluster);
        if (Boolean.FALSE.equals(enabledObservation)) {
            return doExecute(producer, executor, cluster, msg);
        }

        RocketSenderObservationContext context =
            new RocketSenderObservationContext(cluster, topic, tag);

        return observationExecutor.execute(
            CONVENTION,
            () -> context,
            () -> doExecute(producer, executor, cluster, msg)
        );
    }

    private void validate(String cluster, String topic, Object message) {
        Objects.requireNonNull(cluster, "cluster must not be null");
        Objects.requireNonNull(topic, "topic must not be null");
        Objects.requireNonNull(message, "message must not be null");
    }

    private Message buildMessage(String topic, String tag,
        Object message, Map<String, Object> headers) {
        byte[] body = serialize(message);
        Message msg = new Message(topic, tag, body);
        // 设置唯一消息Key（用于查询/排查/去重）
        msg.setKeys(IdUtil.simpleUUID());
        traceContextPropagator.inject(
            msg,
            ROCKET_SETTER
        );
        if (MapUtil.isNotEmpty(headers)) {
            headers.forEach((k, v) ->
                msg.putUserProperty(k, String.valueOf(v))
            );
        }

        return msg;
    }

    private byte[] serialize(Object message) {
        if (message == null) {
            return new byte[0];
        }

        // byte[] 直接透传
        if (message instanceof byte[] bytes) {
            return bytes;
        }

        // String 优化（避免多一次 JSON 包装）
        if (message instanceof String str) {
            return str.getBytes(StandardCharsets.UTF_8);
        }

        return JacksonUtil.toJsonBytes(message);
    }

    private <T> T doExecute(DefaultMQProducer producer, RocketSendExecutor<T> executor, String cluster, Message msg) {
        try {
            final T result = executor.execute(producer, msg);
            log.info("Rocket send success, cluster={}, topic={}, tag={}, keys={}",
                cluster, msg.getTopic(), msg.getTags(), msg.getKeys()
            );
            return result;
        } catch (Exception e) {
            throw wrapException(cluster, msg, e);
        }
    }

    private RuntimeException wrapException(String cluster, Message msg, Exception e) {
        log.error("Rocket send failed, cluster={}, topic={}, tag={}, keys={}",
            cluster, msg.getTopic(), msg.getTags(), msg.getKeys(), e
        );
        return new RuntimeException(e);
    }

    /**
     * 统一发送执行器
     */
    @FunctionalInterface
    public interface RocketSendExecutor<T> {

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
