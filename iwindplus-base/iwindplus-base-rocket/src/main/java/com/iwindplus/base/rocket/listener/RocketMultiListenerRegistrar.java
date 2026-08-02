/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.listener;

import com.iwindplus.base.rocket.core.RocketClusterManager;
import com.iwindplus.base.rocket.domain.dto.RocketMultiListenerMetaDTO;
import com.iwindplus.base.rocket.support.RocketListenerInvoker;
import com.iwindplus.base.rocket.support.RocketMessageHandler;
import com.iwindplus.base.rocket.support.RocketReceiverDispatcher;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;

/**
 * Rocket统一注册器.
 *
 * @author zengdegui
 * @since 2026/03/26 00:58
 */
@Slf4j
@RequiredArgsConstructor
public class RocketMultiListenerRegistrar implements SmartLifecycle, DisposableBean {

    private final RocketMultiListenerBeanPostProcessor bpp;
    private final RocketListenerInvoker listenerInvoker;
    private final RocketClusterManager clusterManager;
    private final RocketReceiverDispatcher dispatcher;

    private final Map<String, DefaultMQPushConsumer> consumersMap = new ConcurrentHashMap<>(16);

    private volatile boolean running;

    @Override
    public void start() {
        final List<RocketMultiListenerMetaDTO> metas = bpp.getMetadata().stream().map(this::resolve).toList();
        if (metas.isEmpty()) {
            log.warn("No Rocket listeners found");
            return;
        }

        final List<RocketMultiListenerMetaDTO> dataList = listenerInvoker.listGroupMergePreWarm(metas);
        registerAll(dataList);

        running = true;
    }

    @Override
    public void stop() {
        running = false;

        consumersMap.forEach((key, consumer) -> {
            try {
                consumer.shutdown();
                log.info(
                    "Rocket consumer shutdown {}",
                    key
                );
            } catch (Exception e) {
                log.error(
                    "Rocket consumer shutdown failed {}",
                    key,
                    e
                );
            }
        });

        consumersMap.clear();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE + 100;
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }

    private void registerAll(List<RocketMultiListenerMetaDTO> metas) {
        int count = 0;

        for (RocketMultiListenerMetaDTO meta : metas) {
            register(meta);

            count++;
        }

        log.info("Rocket listeners registered, sourceSize={}, listenerSize={}", metas.size(), count);
    }

    private RocketMultiListenerMetaDTO resolve(RocketMultiListenerMetaDTO m) {
        String cluster = m.getCluster() != null
            ? m.getCluster()
            : clusterManager.getDefaultCluster();

        return RocketMultiListenerMetaDTO.builder()
            .bean(m.getBean())
            .method(m.getMethod())
            .cluster(cluster)
            .topic(m.getTopic())
            .tag(m.getTag())
            .group(clusterManager.getGroup(cluster, m.getGroup()))
            .orderly(m.getOrderly())
            .build();
    }

    private void register(RocketMultiListenerMetaDTO meta) {
        String listenerId = meta.getListenerId();
        if (consumersMap.containsKey(listenerId)) {
            log.warn("Rocket consumer already started, listenerId={}", listenerId);
            return;
        }

        final DefaultMQPushConsumer consumer = clusterManager.getConsumer(meta.getCluster(), meta.getGroup());
        if (consumer == null) {
            throw new IllegalStateException("Rocket consumer not found, cluster=" + meta.getCluster());
        }

        try {
            consumer.subscribe(meta.getTopic(), meta.getTag());
            registerListener(meta, consumer);

            consumersMap.put(listenerId, consumer);

            consumer.start();

            log.info(
                "Rocket consumer started, cluster={}, group={}, orderly={}, listenerId={}",
                meta.getCluster(),
                meta.getGroup(),
                meta.getOrderly(),
                listenerId
            );
        } catch (Exception e) {
            log.error(
                "Rocket consumer start failed, cluster={}, group={}, orderly={}, listenerId={}",
                meta.getCluster(),
                meta.getGroup(),
                meta.getOrderly(),
                listenerId,
                e
            );

            consumersMap.remove(listenerId);
            throw new RuntimeException(e);
        }
    }

    private void registerListener(
        RocketMultiListenerMetaDTO meta,
        DefaultMQPushConsumer consumer) {
        if (meta.getOrderly()) {
            consumer.registerMessageListener(
                (MessageListenerOrderly)
                    (msgs, ctx) ->
                        dispatchOrderly(meta, msgs, ctx)
            );

        } else {
            consumer.registerMessageListener(
                (MessageListenerConcurrently)
                    (msgs, ctx) ->
                        dispatchConcurrently(meta, msgs, ctx)
            );
        }
    }

    private ConsumeConcurrentlyStatus dispatchConcurrently(
        RocketMultiListenerMetaDTO meta,
        List<MessageExt> msgs,
        ConsumeConcurrentlyContext context) {
        try {
            doDispatch(meta, msgs);
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            log.error(
                "Rocket concurrently consume failed, topic={}, msgs={}",
                meta.getTopic(),
                msgs.stream()
                    .map(MessageExt::getMsgId)
                    .toList(),
                e
            );

            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    private ConsumeOrderlyStatus dispatchOrderly(
        RocketMultiListenerMetaDTO meta,
        List<MessageExt> msgs,
        ConsumeOrderlyContext context) {
        try {
            doDispatch(meta, msgs);
            return ConsumeOrderlyStatus.SUCCESS;
        } catch (Exception e) {
            log.error(
                "Rocket orderly consume failed, topic={}, msgs={}",
                meta.getTopic(),
                msgs.stream()
                    .map(MessageExt::getMsgId)
                    .toList(),
                e
            );

            return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
        }
    }

    private void doDispatch(RocketMultiListenerMetaDTO meta, List<MessageExt> msgs) {
        dispatcher.dispatch(
            new RocketMessageHandler(meta.getCluster(), meta.getTopic(), meta.getGroup(), meta.getTag(),
                msgs, meta.getOrderly(),
                ignored -> listenerInvoker.invoke(meta, msgs)));
    }
}