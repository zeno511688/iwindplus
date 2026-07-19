/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.listener;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.SecureUtil;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.rabbit.core.RabbitClusterManager;
import com.iwindplus.base.rabbit.domain.constant.RabbitConstant;
import com.iwindplus.base.rabbit.domain.dto.RabbitConsumerKeyDTO;
import com.iwindplus.base.rabbit.domain.dto.RabbitMultiListenerMetaDTO;
import com.iwindplus.base.rabbit.support.RabbitMessageHandler;
import com.iwindplus.base.rabbit.support.RabbitReceiverDispatcher;
import com.iwindplus.base.util.JacksonUtil;
import com.rabbitmq.client.Channel;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    private final RabbitClusterManager clusterManager;
    private final RabbitReceiverDispatcher dispatcher;

    private final Map<Method, BeanInvoker> invokerCache = new ConcurrentHashMap<>(16);
    private final Map<Method, ArgMetadata[]> argCache = new ConcurrentHashMap<>(16);
    private final Cache<Class<?>, ObjectReader> readerCache = Caffeine.newBuilder().maximumSize(1024).build();
    private final Map<String, SimpleMessageListenerContainer> containerMap = new ConcurrentHashMap<>(16);

    private volatile boolean running;

    @Override
    public void start() {
        var metas = bpp.getMetadata().stream().map(this::resolve).toList();
        if (metas.isEmpty()) {
            log.warn("No Rabbit listeners found");
            return;
        }

        preWarm(metas);
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

        invokerCache.clear();
        argCache.clear();
        readerCache.invalidateAll();
    }

    private void preWarm(List<RabbitMultiListenerMetaDTO> metas) {
        for (var meta : metas) {
            Method m = meta.getMethod();
            invokerCache.computeIfAbsent(m, x -> createInvoker(m, meta.getBean()));
            argCache.computeIfAbsent(m, this::buildArgMetadata);
            warmReader(m);
        }
    }

    private void warmReader(Method m) {
        Class<?>[] t = m.getParameterTypes();
        Type[] g = m.getGenericParameterTypes();
        for (int i = 0; i < t.length; i++) {
            if (t[i] == Message.class || t[i] == Channel.class) {
                continue;
            }
            Class<?> c = List.class.isAssignableFrom(t[i]) ? extractGeneric(g[i]) : t[i];
            readerCache.get(c, k -> JacksonUtil.getMapper().readerFor(k));
        }
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
        String id = buildId(meta);
        if (containerMap.containsKey(id)) {
            log.warn("Rabbit listener already started, id={}", id);
            return;
        }

        SimpleRabbitListenerContainerFactory factory = clusterManager.getFactory(meta.getCluster());
        if (factory == null) {
            throw new IllegalStateException("Rabbit listener factory not found, cluster=" + meta.getCluster());
        }

        SimpleMessageListenerContainer container = factory.createListenerContainer();
        container.setListenerId(id);
        container.setQueueNames(meta.getQueues());
        container.setMessageListener(createListener(meta));

        try {
            container.start();

            containerMap.put(id, container);

            log.info("Rabbit listener started, cluster={}, group={}, queues={}, id={}",
                meta.getCluster(),
                meta.getGroup(),
                meta.getQueues(),
                id
            );
        } catch (Exception e) {
            log.error(
                "Rabbit listener start failed, cluster={}, group={}, queues={}",
                meta.getCluster(),
                meta.getGroup(),
                meta.getQueues(),
                e
            );

            containerMap.remove(id);
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
            new RabbitMessageHandler(meta.getCluster(), meta.getQueues(), meta.getGroup(), ignored -> invoke(meta, messages, channel)), messages);
    }

    private void invoke(RabbitMultiListenerMetaDTO meta, List<Message> messages, Channel channel) {
        Method method = meta.getMethod();
        ArgMetadata[] metadata = argCache.computeIfAbsent(method, this::buildArgMetadata);
        Object[] args = new Object[metadata.length];
        for (int i = 0; i < metadata.length; i++) {
            args[i] = buildArg(metadata[i], messages, channel);
        }
        try {
            invokerCache.computeIfAbsent(method, m -> createInvoker(method, meta.getBean())).invoke(args);
        } catch (Throwable e) {
            log.error(
                "Rabbit listener invoke failed, cluster={}, group={}, queues={}, method={}",
                meta.getCluster(),
                meta.getGroup(),
                meta.getQueues(),
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
        Class<?>[] types = m.getParameterTypes();
        Type[] generics = m.getGenericParameterTypes();
        ArgMetadata[] arr = new ArgMetadata[types.length];
        for (int i = 0; i < types.length; i++) {
            arr[i] = buildArgMetadata(types[i], generics[i]);
        }
        return arr;
    }

    private ArgMetadata buildArgMetadata(Class<?> type, Type generic) {
        if (type == Message.class) {
            return new ArgMetadata(ArgType.MSG, null);
        }
        if (type == Channel.class) {
            return new ArgMetadata(ArgType.CHANNEL, null);
        }
        if (List.class.isAssignableFrom(type)) {
            Class<?> clazz = extractGeneric(generic);
            return clazz == Message.class ? new ArgMetadata(ArgType.MSG_LIST, null) : new ArgMetadata(ArgType.DTO_LIST, getReader(clazz));
        }
        return new ArgMetadata(ArgType.DTO, getReader(type));
    }

    private Object buildArg(ArgMetadata meta, List<Message> messages, Channel channel) {
        return switch (meta.type) {
            case MSG -> messages.get(0);
            case CHANNEL -> channel;
            case MSG_LIST -> messages;
            case DTO -> read(messages.get(0), meta.reader);
            case DTO_LIST -> messages.stream().map(m -> read(m, meta.reader)).toList();
        };
    }

    private ObjectReader getReader(Class<?> clazz) {
        return readerCache.get(clazz, c -> JacksonUtil.getMapper().readerFor(c));
    }

    private Object read(Message message, ObjectReader reader) {
        try {
            return reader.readValue(message.getBody());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> extractGeneric(Type type) {
        if (type instanceof ParameterizedType pt) {
            return (Class<?>) pt.getActualTypeArguments()[0];
        }
        throw new IllegalArgumentException("Unsupported generic type");
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
        Map<RabbitConsumerKeyDTO, List<RabbitMultiListenerMetaDTO>> grouped = new HashMap<>(16);

        for (RabbitMultiListenerMetaDTO meta : metas) {
            RabbitConsumerKeyDTO key = new RabbitConsumerKeyDTO(meta.getCluster(), meta.getGroup());

            grouped.computeIfAbsent(key, k -> new ArrayList<>(10))
                .add(meta);
        }

        return grouped;
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

    enum ArgType {MSG, MSG_LIST, DTO, DTO_LIST, CHANNEL}

    record ArgMetadata(ArgType type, ObjectReader reader) {

    }

    @FunctionalInterface
    interface BeanInvoker {

        /**
         * Invoke the method with the given arguments.
         *
         * @param args the arguments to invoke the method with
         * @return the result of the method invocation
         * @throws Throwable if the method invocation failed
         */
        Object invoke(Object[] args) throws Throwable;
    }
}