/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.core;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.kafka.support.KafkaSenderDispatcher;
import com.iwindplus.base.kafka.support.KafkaSenderDispatcher.KafkaSendExecutor;
import com.iwindplus.base.kafka.support.KafkaSenderDispatcher.ReactiveKafkaSendExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import io.opentelemetry.context.Context;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

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

        final Message<String> objectMessage = buildTraceMessage(topic, key, message);
        return dispatcher.dispatch(
            cluster,
            topic,
            key,
            headers,
            message,
            (KafkaSendExecutor<SendResult<String, Object>>) (template, msg) -> {
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
     * Reactive发送.
     *
     * @param cluster 集群
     * @param topic   topic
     * @param message 消息
     * @return Mono<SenderResult < Void>>
     */
    public Mono<SenderResult<Void>> sendReactive(
        String cluster,
        String topic,
        Map<String, Object> headers,
        String message) {
        return sendReactive(cluster, topic, null, headers, message);
    }

    /**
     * Reactive发送.
     *
     * @param cluster 集群
     * @param topic   topic
     * @param key     key
     * @param message 消息
     * @return Mono<SenderResult < Void>>
     */
    public Mono<SenderResult<Void>> sendReactive(
        String cluster,
        String topic,
        String key,
        Map<String, Object> headers,
        String message) {

        return Mono.deferContextual(
            reactorContext -> {
                Context context =
                    reactorContext.getOrDefault(
                        TraceContextPropagator
                            .TRACE_CONTEXT_KEY,
                        Context.current()
                    );

                Message<String> traceMessage =
                    buildReactiveTraceMessage(
                        topic,
                        key,
                        message,
                        context
                    );

                return dispatcher.dispatch(
                    cluster,
                    topic,
                    key,
                    headers,
                    message,
                    (ReactiveKafkaSendExecutor<Mono<SenderResult<Void>>>) (template, msg) ->
                        template.send(msg.getTopic(), traceMessage)
                            .doOnNext(result ->
                                logReactiveSuccess(
                                    msg.getCluster(),
                                    msg.getTopic(),
                                    msg.getKey(),
                                    result
                                )
                            ).doOnError(e ->
                                logReactiveError(
                                    msg.getCluster(),
                                    msg.getTopic(),
                                    msg.getKey(),
                                    e
                                )
                            )
                );
            }
        );
    }

    /**
     * Reactive 批量发送.
     *
     * @param cluster     集群
     * @param topic       topic
     * @param messages    消息
     * @param concurrency 并发数
     * @return Mono<Void>
     */
    public Mono<Void> sendBatchReactive(
        String cluster,
        String topic,
        Map<String, Object> headers,
        List<String> messages,
        int concurrency) {

        return Flux.fromIterable(messages)
            .flatMap(
                msg -> sendReactive(cluster, topic, headers, msg),
                concurrency
            )
            .then();
    }

    /**
     * 构建同步Trace消息.
     */
    private Message<String> buildTraceMessage(
        String topic,
        String key,
        String message) {

        MessageBuilder<String> builder =
            MessageBuilder.withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, topic);

        if (CharSequenceUtil.isNotBlank(key)) {
            builder.setHeader(KafkaHeaders.KEY, key);
        }

        traceContextPropagator.inject(
            builder,
            MessageBuilder::setHeader
        );

        return builder.build();
    }

    /**
     * 构建Reactive Trace消息.
     */
    private Message<String> buildReactiveTraceMessage(
        String topic,
        String key,
        String message,
        Context context) {

        MessageBuilder<String> builder =
            MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, topic);

        if (CharSequenceUtil.isNotBlank(key)) {
            builder.setHeader(KafkaHeaders.KEY, key);
        }

        traceContextPropagator.injectReactor(
            context,
            builder,
            MessageBuilder::setHeader
        );

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

    /**
     * Reactive发送成功日志.
     */
    private void logReactiveSuccess(
        String cluster,
        String topic,
        String key,
        SenderResult<Void> result) {

        RecordMetadata metadata = result.recordMetadata();
        if (metadata == null) {
            return;
        }

        log.debug(
            "Reactive send success cluster={} topic={} key={} partition={} offset={}",
            cluster,
            topic,
            key,
            metadata.partition(),
            metadata.offset()
        );
    }

    /**
     * Reactive发送失败日志.
     */
    private void logReactiveError(
        String cluster,
        String topic,
        String key,
        Throwable e) {

        log.error(
            "Reactive send failed cluster={} topic={} key={} err={}",
            cluster,
            topic,
            key,
            e.getMessage(),
            e
        );
    }
}