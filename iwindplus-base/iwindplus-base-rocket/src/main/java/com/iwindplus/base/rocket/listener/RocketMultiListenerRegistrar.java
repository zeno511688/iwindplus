/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.listener;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.rocket.core.RocketClusterManager;
import com.iwindplus.base.rocket.domain.dto.RocketConsumerKeyDTO;
import com.iwindplus.base.rocket.domain.dto.RocketMultiListenerMetaDTO;
import com.iwindplus.base.rocket.domain.dto.RocketTopicTagKeyDTO;
import com.iwindplus.base.rocket.support.RocketMessageHandler;
import com.iwindplus.base.rocket.support.RocketReceiverDispatcher;
import com.iwindplus.base.util.JacksonUtil;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
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
    private final RocketClusterManager clusterManager;
    private final RocketReceiverDispatcher dispatcher;

    private final Map<Method, BeanInvoker> invokerCache = new ConcurrentHashMap<>(16);
    private final Map<Method, ArgMetadata[]> argCache = new ConcurrentHashMap<>(16);

    private final Cache<Class<?>, ObjectReader> readerCache =
        Caffeine.newBuilder().maximumSize(1024).build();

    private final Map<RocketConsumerKeyDTO, Map<RocketTopicTagKeyDTO, List<RocketMessageHandler>>> index =
        new ConcurrentHashMap<>(16);

    private final Map<RocketConsumerKeyDTO, DefaultMQPushConsumer> consumersMap = new ConcurrentHashMap<>(16);

    private volatile boolean running;

    @Override
    public void start() {
        var metas = bpp.getMetadata().stream().map(this::resolve).toList();
        if (metas.isEmpty()) {
            log.warn("No Rocket listeners found");
            return;
        }

        preWarm(metas);
        registerAll(metas);
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
        invokerCache.clear();
        argCache.clear();
        readerCache.invalidateAll();
        index.clear();
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

    private void preWarm(List<RocketMultiListenerMetaDTO> metas) {
        for (var meta : metas) {
            Method m = meta.getMethod();

            invokerCache.computeIfAbsent(m, x -> createInvoker(m, meta.getBean()));
            argCache.computeIfAbsent(m, this::buildArgMetadata);
            warmReader(m);
        }
    }

    private void warmReader(Method m) {
        var t = m.getParameterTypes();
        var g = m.getGenericParameterTypes();

        for (int i = 0; i < t.length; i++) {
            if (t[i] == MessageExt.class) {
                continue;
            }

            Class<?> c = List.class.isAssignableFrom(t[i])
                ? extract(g[i])
                : t[i];

            readerCache.get(c, k -> JacksonUtil.getMapper().readerFor(k));
        }
    }

    private void registerAll(List<RocketMultiListenerMetaDTO> metas) {
        int count = 0;
        for (var m : metas) {
            register(m);
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
        var key = new RocketConsumerKeyDTO(meta.getCluster(), meta.getGroup());
        var tt = new RocketTopicTagKeyDTO(meta.getTopic(), meta.getTag());

        if (consumersMap.containsKey(key)) {
            log.warn("Rocket consumer already started, key={}", key);
            return;
        }

        index.computeIfAbsent(
                key,
                k -> new ConcurrentHashMap<>()
            )
            .computeIfAbsent(
                tt,
                k -> new CopyOnWriteArrayList<>()
            )
            .add(buildHandler(meta));

        var consumer = clusterManager.getConsumer(meta.getCluster(), meta.getGroup());
        if (consumer == null) {
            throw new IllegalStateException("Rocket consumer not found, cluster=" + meta.getCluster());
        }

        try {
            consumer.subscribe(meta.getTopic(), meta.getTag());
            registerListener(meta, key, consumer);

            consumersMap.put(key, consumer);

            consumer.start();

            log.info(
                "Rocket consumer started, cluster={}, group={}, orderly={}",
                meta.getCluster(),
                meta.getGroup(),
                meta.getOrderly()
            );
        } catch (Exception e) {
            log.error(
                "Rocket consumer start failed, cluster={}, group={}, orderly={}",
                meta.getCluster(),
                meta.getGroup(),
                meta.getOrderly(),
                e
            );

            consumersMap.remove(key);
            throw new RuntimeException(e);
        }
    }

    private void registerListener(
        RocketMultiListenerMetaDTO meta,
        RocketConsumerKeyDTO key,
        DefaultMQPushConsumer consumer) {
        if (meta.getOrderly()) {
            consumer.registerMessageListener(
                (MessageListenerOrderly)
                    (msgs, ctx) ->
                        dispatchOrderly(key, msgs)
            );

        } else {
            consumer.registerMessageListener(
                (MessageListenerConcurrently)
                    (msgs, ctx) ->
                        dispatchConcurrently(key, msgs)
            );
        }
    }

    private RocketMessageHandler buildHandler(RocketMultiListenerMetaDTO m) {
        return new RocketMessageHandler(
            m.getCluster(), m.getTopic(), m.getTag(), m.getGroup(), m.getOrderly(),
            msgs -> invoke(m, msgs)
        );
    }

    private ConsumeConcurrentlyStatus dispatchConcurrently(RocketConsumerKeyDTO k, List<MessageExt> msgs) {
        try {
            doDispatch(k, msgs);
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            log.error(
                "Rocket concurrently consume failed, key={}, msgs={}",
                k,
                msgs.stream()
                    .map(MessageExt::getMsgId)
                    .toList(),
                e
            );

            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    private ConsumeOrderlyStatus dispatchOrderly(RocketConsumerKeyDTO k, List<MessageExt> msgs) {
        try {
            doDispatch(k, msgs);
            return ConsumeOrderlyStatus.SUCCESS;
        } catch (Exception e) {
            log.error(
                "Rocket orderly consume failed, key={}, msgs={}",
                k,
                msgs.stream()
                    .map(MessageExt::getMsgId)
                    .toList(),
                e
            );

            return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
        }
    }

    private void doDispatch(RocketConsumerKeyDTO k, List<MessageExt> msgs) {
        var map = index.get(k);
        if (map == null) {
            return;
        }

        if (CollUtil.isEmpty(msgs)) {
            return;
        }
        var first = msgs.get(0);

        var key = new RocketTopicTagKeyDTO(first.getTopic(), first.getTags());

        var handlers = map.get(key);
        if (handlers == null) {
            return;
        }

        for (var h : handlers) {
            dispatcher.dispatch(h, msgs);
        }
    }

    private Object invoke(RocketMultiListenerMetaDTO m, List<MessageExt> msgs) {
        Method method = m.getMethod();
        var meta = argCache.computeIfAbsent(method, this::buildArgMetadata);

        Object[] args = new Object[meta.length];
        for (int i = 0; i < meta.length; i++) {
            args[i] = buildArg(meta[i], msgs);
        }

        try {
            return invokerCache.computeIfAbsent(method, x -> createInvoker(method, m.getBean()))
                .invoke(args);
        } catch (Throwable e) {
            log.error(
                "Rocket listener invoke failed, cluster={}, group={}, topic={}, method={}",
                m.getCluster(),
                m.getGroup(),
                m.getTopic(),
                method,
                e
            );
            throw new RuntimeException(e);
        }
    }

    private BeanInvoker createInvoker(Method m, Object bean) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(bean.getClass(), MethodHandles.lookup());
            MethodHandle handle = lookup.unreflect(m).bindTo(bean);
            return handle.asSpreader(Object[].class, m.getParameterCount())::invoke;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private ArgMetadata[] buildArgMetadata(Method m) {
        var t = m.getParameterTypes();
        var g = m.getGenericParameterTypes();

        ArgMetadata[] r = new ArgMetadata[t.length];

        for (int i = 0; i < t.length; i++) {
            r[i] = buildArgMeta(t[i], g[i]);
        }

        return r;
    }

    private ArgMetadata buildArgMeta(Class<?> t, Type g) {
        if (t == MessageExt.class) {
            return new ArgMetadata(ArgType.MSG, null);
        }

        if (List.class.isAssignableFrom(t)) {
            Class<?> c = extract(g);

            return c == MessageExt.class
                ? new ArgMetadata(ArgType.MSG_LIST, null)
                : new ArgMetadata(ArgType.DTO_LIST, getReader(c));
        }

        return new ArgMetadata(ArgType.DTO, getReader(t));
    }

    private Object buildArg(ArgMetadata m, List<MessageExt> msgs) {
        return switch (m.type) {
            case MSG -> msgs.get(0);
            case MSG_LIST -> msgs;
            case DTO -> read(msgs.get(0), m.reader);
            case DTO_LIST -> msgs.stream().map(x -> read(x, m.reader)).toList();
        };
    }

    private ObjectReader getReader(Class<?> c) {
        return readerCache.get(c, x -> JacksonUtil.getMapper().readerFor(x));
    }

    private Object read(MessageExt m, ObjectReader r) {
        try {
            return r.readValue(m.getBody());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> extract(Type t) {
        if (t instanceof ParameterizedType p) {
            return (Class<?>) p.getActualTypeArguments()[0];
        }
        throw new IllegalArgumentException();
    }

    enum ArgType {MSG, MSG_LIST, DTO, DTO_LIST}

    record ArgMetadata(ArgType type, ObjectReader reader) {

    }

    @FunctionalInterface
    interface BeanInvoker {

        /**
         * Invoke the method with the given arguments.
         *
         * @param args the arguments to invoke the method with
         * @return the invocation result
         * @throws Throwable in case of invocation failure
         */
        Object invoke(Object[] args) throws Throwable;
    }
}