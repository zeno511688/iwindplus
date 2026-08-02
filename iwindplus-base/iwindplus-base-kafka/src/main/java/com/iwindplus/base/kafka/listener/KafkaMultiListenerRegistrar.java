/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.listener;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.kafka.core.KafkaClusterManager;
import com.iwindplus.base.kafka.domain.dto.KafkaMultiListenerMetaDTO;
import com.iwindplus.base.kafka.domain.event.KafkaDisruptorEvent;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty;
import com.iwindplus.base.kafka.support.KafkaListenerInvoker;
import com.iwindplus.base.kafka.support.KafkaMessageHandler;
import com.iwindplus.base.kafka.support.KafkaReceiverDispatcher;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.AcknowledgingConsumerAwareMessageListener;
import org.springframework.kafka.listener.BatchAcknowledgingConsumerAwareMessageListener;
import org.springframework.kafka.listener.BatchConsumerAwareMessageListener;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ConsumerAwareMessageListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.support.Acknowledgment;

/**
 * kafka统一注册器.
 *
 * @author zengdegui
 * @since 2026/03/26 00:58
 */
@Slf4j
@RequiredArgsConstructor
public class KafkaMultiListenerRegistrar implements SmartLifecycle, DisposableBean {

    private final ApplicationContext applicationContext;
    private final KafkaMultiListenerBeanPostProcessor bpp;
    private final KafkaListenerInvoker listenerInvoker;
    private final KafkaClusterManager clusterManager;
    private final KafkaReceiverDispatcher dispatcher;

    private final Map<String, ConcurrentMessageListenerContainer<String, Object>> containerMap = new ConcurrentHashMap<>(16);

    private volatile boolean running;

    @Override
    public void start() {
        List<KafkaMultiListenerMetaDTO> metas = bpp.getMetadata().stream().map(this::resolve).toList();
        if (metas.isEmpty()) {
            log.warn("No Kafka listeners found");
            return;
        }

        final List<KafkaMultiListenerMetaDTO> dataList = listenerInvoker.listGroupMergePreWarm(metas);
        registerAll(dataList);

        running = true;
    }

    @Override
    public void stop() {
        running = false;

        containerMap.forEach((id, c) -> {
            try {
                c.stop();
                c.destroy();
                log.info("Kafka listener stopped: {}", id);
            } catch (Exception e) {
                log.error("Stop kafka listener failed: {}", id, e);
            }
        });
    }

    @Override
    public void destroy() {
        stop();

        containerMap.clear();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE + 100;
    }

    /**
     * 获取所有监听器.
     *
     * @return Map<String, ConcurrentMessageListenerContainer < String, Object>>
     */
    public Map<String, ConcurrentMessageListenerContainer<String, Object>> getContainerMap() {
        return containerMap;
    }

    private void registerAll(List<KafkaMultiListenerMetaDTO> metas) {

        int count = 0;

        for (KafkaMultiListenerMetaDTO meta : metas) {
            register(meta);

            count++;
        }

        log.info("Kafka listeners registered, sourceSize={}, listenerSize={}", metas.size(), count);
    }

    private void register(KafkaMultiListenerMetaDTO meta) {
        String listenerId = meta.getListenerId();
        if (containerMap.containsKey(listenerId)) {
            log.warn("Kafka listener already started, listenerId={}", listenerId);
            return;
        }

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            clusterManager.getFactory(meta.getCluster());
        if (factory == null) {
            throw new IllegalStateException("Kafka listener factory not found, cluster=" + meta.getCluster());
        }

        String clusterId = clusterManager.getClusterId(meta.getCluster());
        KafkaMultiProperty property = clusterManager.getProperty();
        ConcurrentMessageListenerContainer<String, Object> container =
            factory.createContainer(meta.getTopics());
        container.setBeanName(listenerId);
        container.setApplicationEventPublisher(applicationContext);
        ContainerProperties p = container.getContainerProperties();
        final String clientId = CharSequenceUtil.isNotBlank(p.getClientId())
            ? p.getClientId() : clusterManager.getConsumerClientId(meta.getCluster());
        //开启空闲事件
        p.setIdleEventInterval(Duration.ofSeconds(5).toMillis());
        p.setIdleBeforeDataMultiplier(1);

        registerListener(clusterId, listenerId, clientId, meta, property, p);

        try {
            container.start();

            containerMap.put(listenerId, container);

            log.info("Kafka listener started, cluster={}, group={}, topics={}, listenerId={}",
                meta.getCluster(),
                meta.getGroup(),
                meta.getTopics(),
                listenerId
            );
        } catch (Exception e) {
            log.error(
                "Kafka listener start failed, cluster={}, group={}, topics={}, listenerId={}",
                meta.getCluster(),
                meta.getGroup(),
                meta.getTopics(),
                listenerId,
                e
            );

            containerMap.remove(listenerId);

            throw new RuntimeException("Kafka listener start failed", e);
        }
    }

    private void registerListener(
        String clusterId,
        String listenerId,
        String clientId,
        KafkaMultiListenerMetaDTO meta,
        KafkaMultiProperty property,
        ContainerProperties p) {
        p.setGroupId(meta.getGroup());

        boolean batch = property.getEnabledBatchListener(meta.getCluster());
        final AckMode ackMode = p.getAckMode();
        boolean manualAck = AckMode.MANUAL.equals(ackMode) || AckMode.MANUAL_IMMEDIATE.equals(ackMode);
        // 批处理
        if (batch) {
            if (manualAck) {
                p.setMessageListener((BatchAcknowledgingConsumerAwareMessageListener<String, Object>)
                    (records, ack, consumer) -> dispatchWithCommit(clusterId, listenerId, clientId, meta,
                        records, ack, consumer)
                );
            } else {
                p.setMessageListener((BatchConsumerAwareMessageListener<String, Object>)
                    (records, consumer) -> dispatchWithCommit(clusterId, listenerId, clientId, meta,
                        records, null, consumer)
                );
            }
            return;
        } else {
            // 单处理
            if (manualAck) {
                p.setMessageListener((AcknowledgingConsumerAwareMessageListener<String, Object>)
                    (record, ack, consumer) -> dispatchWithCommit(clusterId, listenerId, clientId, meta,
                        Collections.singletonList(record), ack, consumer)
                );
            } else {
                p.setMessageListener((ConsumerAwareMessageListener<String, Object>)
                    (record, consumer) -> dispatchWithCommit(clusterId, listenerId, clientId, meta,
                        Collections.singletonList(record), null, consumer)
                );
            }
        }
    }

    private void dispatchWithCommit(
        String clusterId,
        String listenerId,
        String clientId,
        KafkaMultiListenerMetaDTO meta,
        List<ConsumerRecord<String, Object>> messages,
        Acknowledgment acknowledgment,
        Consumer<?, ?> consumer) {

        // 消息发送到disruptor时使用
        final KafkaDisruptorEvent event = KafkaDisruptorEvent.builder()
            .listenerId(listenerId)
            .messages(messages)
            .successCallbackHandler(records -> {
                // 开启了 asyncAcks;
                // 虽然还是disruptor调用，但是 Spring Kafka 会缓存 ack，并由 Consumer线程最终提交
                ack(acknowledgment);
            })
            .build();

        dispatcher.dispatch(
            new KafkaMessageHandler(
                clusterId,
                listenerId,
                clientId,
                meta.getCluster(),
                meta.getTopics(),
                meta.getGroup(),
                messages,
                // 业务执行
                records -> listenerInvoker.invoke(meta, records, acknowledgment, consumer),
                // 业务成功
                records -> {
                    // 同步调用使用
                    ack(acknowledgment);
                },
                event
            ),
            consumer
        );
    }

    private void ack(Acknowledgment acknowledgment) {
        if (acknowledgment != null) {
            acknowledgment.acknowledge();
        }
    }

    private KafkaMultiListenerMetaDTO resolve(KafkaMultiListenerMetaDTO meta) {
        String cluster = CharSequenceUtil.isBlank(meta.getCluster())
            ? clusterManager.getDefaultCluster()
            : meta.getCluster();

        KafkaMultiListenerMetaDTO resolved = KafkaMultiListenerMetaDTO
            .builder()
            .bean(meta.getBean())
            .method(meta.getMethod())
            .cluster(cluster)
            .topics(meta.getTopics())
            .group(meta.getGroup())
            .build();

        return resolved;
    }
}