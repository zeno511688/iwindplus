/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.core;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant.SystemConstant;
import com.iwindplus.base.kafka.domain.constant.KafkaConstant;
import com.iwindplus.base.kafka.support.KafkaSenderDispatcher;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * Kafka模板路由器.
 *
 * @author zengdegui
 * @since 2026/03/20 21:56
 */
@Slf4j
public record KafkaTemplateRouter(
    KafkaClusterManager manager,
    KafkaSenderDispatcher dispatcher,
    TraceContextPropagator traceContextPropagator) {

    /**
     * 获取 AdminClient.
     *
     * @param cluster 集群名称
     * @return AdminClient
     */
    public AdminClient getAdmin(String cluster) {
        return manager.getAdmin(cluster);
    }

    /**
     * 同步发送.
     *
     * @param message 消息内容
     * @return SendResult<String, Object>
     */
    public SendResult<String, Object> send(Message<String> message) {
        return this.send(manager.getDefaultCluster(), message);
    }

    /**
     * 同步发送.
     *
     * @param cluster 集群
     * @param topic   topic
     * @param message 消息
     * @return SendResult<String, Object>
     */
    public SendResult<String, Object> send(
        String cluster,
        String topic,
        Map<String, Object> headers,
        String message) {
        return send(cluster, topic, null, headers, message);
    }

    /**
     * 同步发送.
     *
     * @param cluster 集群
     * @param message 消息
     * @return SendResult<String, Object>
     */
    public SendResult<String, Object> send(
        String cluster,
        Message<String> message) {

        String topic = message.getHeaders().get(KafkaHeaders.TOPIC, String.class);
        String key = message.getHeaders().get(KafkaHeaders.KEY, String.class);
        Map<String, Object> headers = new HashMap<>(message.getHeaders());
        return send(cluster, topic, key, headers, message.getPayload());
    }

    /**
     * 同步发送.
     *
     * @param cluster 集群
     * @param topic   topic
     * @param key     key
     * @param message 消息
     * @return SendResult<String, Object>
     */
    public SendResult<String, Object> send(
        String cluster,
        String topic,
        String key,
        Map<String, Object> headers,
        String message) {

        final Message<String> objectMessage = buildTraceMessage(cluster, topic, key, headers, message);
        return dispatcher.dispatch(
            cluster,
            topic,
            key,
            headers,
            message,
            (template, msg) -> {
                try {
                    SendResult<String, Object> result =
                        template.send(objectMessage).get();

                    logResult(
                        result,
                        null,
                        msg.getCluster(),
                        msg.getTopic()
                    );

                    return result;
                } catch (Exception e) {
                    logResult(
                        null,
                        e,
                        msg.getCluster(),
                        msg.getTopic()
                    );

                    throw new RuntimeException(e);
                }
            }
        );
    }

    /**
     * 构建同步Trace消息.
     */
    private Message<String> buildTraceMessage(
        String cluster,
        String topic,
        String key,
        Map<String, Object> headers,
        String message) {

        MessageBuilder<String> builder =
            MessageBuilder.withPayload(message);

        if (MapUtil.isNotEmpty(headers)) {
            builder.copyHeaders(headers);
        }
        if (CharSequenceUtil.isNotBlank(cluster)) {
            builder.setHeader(KafkaConstant.CLUSTER, cluster);
        }
        if (CharSequenceUtil.isNotBlank(topic)) {
            builder.setHeader(KafkaHeaders.TOPIC, topic);
        }
        if (CharSequenceUtil.isNotBlank(key)) {
            builder.setHeader(KafkaHeaders.KEY, key);
        }
        if (MapUtil.isNotEmpty(headers)) {
            final Object requestId = headers.get(SystemConstant.REQUEST_ID);
            if (Objects.nonNull(requestId)) {
                builder.setHeader(SystemConstant.REQUEST_ID, requestId);
            }
        }
        return builder.build();
    }

    /**
     * 同步发送日志.
     */
    private void logResult(
        SendResult<String, Object> result,
        Throwable ex,
        String cluster,
        Object topic) {

        if (ex != null) {
            log.error(
                "Failed to send message to cluster {} topic {}: {}",
                cluster,
                topic,
                ex.getMessage(),
                ex
            );

            return;
        }

        RecordMetadata metadata = result.getRecordMetadata();
        if (metadata == null) {
            return;
        }

        log.debug(
            "Sent message to cluster {} topic {} partition {} offset {}",
            cluster,
            metadata.topic(),
            metadata.partition(),
            metadata.offset()
        );
    }
}