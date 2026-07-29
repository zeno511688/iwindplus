/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.listener;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.SecureUtil;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.rabbit.core.RabbitClusterManager;
import com.iwindplus.base.rabbit.domain.constant.RabbitConstant;
import com.iwindplus.base.rabbit.domain.dto.RabbitConsumerKeyDTO;
import com.iwindplus.base.rabbit.domain.dto.RabbitMultiListenerMetaDTO;
import com.iwindplus.base.rabbit.support.RabbitListenerInvoker;
import com.iwindplus.base.rabbit.support.RabbitMessageHandler;
import com.iwindplus.base.rabbit.support.RabbitReceiverDispatcher;
import com.rabbitmq.client.Channel;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareBatchMessageListener;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;

/**
 * Rabbit统一注册器.
 *
 * @author zengdegui
 * @since 2026/03/26 00:58
 */
@Slf4j
@RequiredArgsConstructor
public class RabbitMultiListenerRegistrar implements SmartLifecycle, DisposableBean {

    private final RabbitMultiListenerBeanPostProcessor bpp;
    private final RabbitListenerInvoker listenerInvoker;
    private final RabbitClusterManager clusterManager;
    private final RabbitReceiverDispatcher dispatcher;

    private final Map<String, SimpleMessageListenerContainer> containerMap = new ConcurrentHashMap<>(16);

    private volatile boolean running;

    @Override
    public void start() {
        var metas = bpp.getMetadata().stream().map(this::resolve).toList();
        if (metas.isEmpty()) {
            log.warn("No Rabbit listeners found");
            return;
        }

        listenerInvoker.preWarm(metas);
        registerAll(metas);
        running = true;
    }

    @Override
    public void stop() {
        running = false;

        containerMap.forEach((id, container) -> {
            try {
                container.stop();
                log.info(
                    "Rabbit listener stopped:{}",
                    id
                );
            } catch (Exception e) {
                log.error(
                    "Stop rabbit listener failed:{}",
                    id,
                    e
                );
            }
        });

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

    @Override
    public void destroy() {
        stop();
    }

    private void registerAll(List<RabbitMultiListenerMetaDTO> metas) {
        Map<RabbitConsumerKeyDTO, List<RabbitMultiListenerMetaDTO>> grouped = group(metas);

        int count = 0;

        for (var entry : grouped.entrySet()) {
            RabbitMultiListenerMetaDTO meta = merge(entry.getValue());

            register(meta);

            count++;
        }

        log.info("Rabbit listeners registered, sourceSize={}, listenerSize={}", metas.size(), count);
    }

    private void register(RabbitMultiListenerMetaDTO meta) {
        String listenerId = buildId(meta);
        if (containerMap.containsKey(listenerId)) {
            log.warn("Rabbit listener already started, listenerId={}", listenerId);
            return;
        }

        SimpleRabbitListenerContainerFactory factory = clusterManager.getFactory(meta.getCluster());
        if (factory == null) {
            throw new IllegalStateException("Rabbit listener factory not found, cluster=" + meta.getCluster());
        }

        SimpleMessageListenerContainer container = factory.createListenerContainer();
        container.setListenerId(listenerId);
        container.setQueueNames(meta.getQueues());
        container.setMessageListener(createListener(meta));

        try {
            container.start();

            containerMap.put(listenerId, container);

            log.info("Rabbit listener started, cluster={}, group={}, queues={}, listenerId={}",
                meta.getCluster(),
                meta.getGroup(),
                meta.getQueues(),
                listenerId
            );
        } catch (Exception e) {
            log.error(
                "Rabbit listener start failed, cluster={}, group={}, queues={}, listenerId={}",
                meta.getCluster(),
                meta.getGroup(),
                meta.getQueues(),
                listenerId,
                e
            );

            containerMap.remove(listenerId);
            throw new RuntimeException("Rabbit listener start failed", e);
        }
    }

    private RabbitMultiListenerMetaDTO merge(List<RabbitMultiListenerMetaDTO> list) {
        RabbitMultiListenerMetaDTO first = list.get(0);

        String[] queues = list.stream()
            .flatMap(x -> Arrays.stream(x.getQueues()))
            .filter(CharSequenceUtil::isNotBlank)
            .distinct()
            .toArray(String[]::new);

        return RabbitMultiListenerMetaDTO
            .builder()
            .bean(first.getBean())
            .method(first.getMethod())
            .cluster(first.getCluster())
            .group(first.getGroup())
            .queues(queues)
            .build();
    }

    private MessageListener createListener(RabbitMultiListenerMetaDTO meta) {
        boolean batch = clusterManager.getEnabledBatchListener(meta.getCluster());
        return batch ? (ChannelAwareBatchMessageListener) (messages, channel) -> dispatch(meta, messages, channel)
            : (ChannelAwareMessageListener) (message, channel) -> dispatch(meta, List.of(message), channel);
    }

    private void dispatch(RabbitMultiListenerMetaDTO meta, List<Message> messages, Channel channel) {
        dispatcher.dispatch(
            new RabbitMessageHandler(meta.getCluster(), meta.getQueues(), meta.getGroup(), messages,
                ignored -> listenerInvoker.invoke(meta, messages, channel)));
    }

    private RabbitMultiListenerMetaDTO resolve(RabbitMultiListenerMetaDTO meta) {
        String cluster = CharSequenceUtil.isBlank(meta.getCluster()) ? clusterManager.getDefaultCluster() : meta.getCluster();
        String group = clusterManager.getGroup(cluster, meta.getGroup());
        return RabbitMultiListenerMetaDTO
            .builder()
            .bean(meta.getBean())
            .method(meta.getMethod())
            .cluster(cluster)
            .group(group)
            .queues(meta.getQueues())
            .build();
    }

    private Map<RabbitConsumerKeyDTO, List<RabbitMultiListenerMetaDTO>> group(List<RabbitMultiListenerMetaDTO> metas) {
        return metas
            .stream()
            .collect(Collectors.groupingBy(
                entity -> new RabbitConsumerKeyDTO(
                    entity.getCluster(),
                    entity.getGroup()
                )
            ));
    }

    private String buildId(RabbitMultiListenerMetaDTO meta) {
        String str = meta.getMethod().toGenericString()
            + SymbolConstant.WELL_NO
            + String.join(SymbolConstant.COMMA, meta.getQueues());

        return RabbitConstant.RABBIT
            + SymbolConstant.HORIZONTAL_LINE + meta.getCluster()
            + SymbolConstant.HORIZONTAL_LINE + meta.getGroup()
            + SymbolConstant.HORIZONTAL_LINE + SecureUtil.md5(str);
    }
}