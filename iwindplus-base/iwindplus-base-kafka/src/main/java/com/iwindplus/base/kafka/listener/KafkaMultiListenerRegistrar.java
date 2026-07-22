/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.listener;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.SecureUtil;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.kafka.core.KafkaClusterManager;
import com.iwindplus.base.kafka.domain.constant.KafkaConstant;
import com.iwindplus.base.kafka.domain.dto.KafkaConsumerKeyDTO;
import com.iwindplus.base.kafka.domain.dto.KafkaMultiListenerMetaDTO;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty;
import com.iwindplus.base.kafka.support.KafkaMessageHandler;
import com.iwindplus.base.kafka.support.KafkaReceiverDispatcher;
import com.iwindplus.base.util.JacksonUtil;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.listener.BatchAcknowledgingMessageListener;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * kafka统一注册器.
 *
 * @author zengdegui
 * @since 2026/03/26 00:58
 */
@Slf4j
@RequiredArgsConstructor
public class KafkaMultiListenerRegistrar implements SmartLifecycle, DisposableBean {

    private final KafkaMultiListenerBeanPostProcessor bpp;
    private final KafkaClusterManager clusterManager;
    private final KafkaReceiverDispatcher dispatcher;

    private final Map<Method, BeanInvoker> invokerCache = new ConcurrentHashMap<>(16);
    private final Map<Method, ArgBuilder[]> argCache = new ConcurrentHashMap<>(16);

    private final Map<String, AbstractMessageListenerContainer<String, Object>> containerMap = new ConcurrentHashMap<>(16);

    private final Cache<Class<?>, ObjectReader> readerCache =
        Caffeine.newBuilder()
            .maximumSize(1024)
            .build();

    private volatile boolean running;

    @Override
    public void start() {
        List<KafkaMultiListenerMetaDTO> metas = bpp.getMetadata().stream().map(this::resolve).toList();
        if (metas.isEmpty()) {
            log.warn("No Kafka listeners found");
            return;
        }

        preWarm(metas);
        registerAll(metas);

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
        invokerCache.clear();
        argCache.clear();
        readerCache.invalidateAll();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE + 100;
    }

    private void preWarm(List<KafkaMultiListenerMetaDTO> metas) {
        for (KafkaMultiListenerMetaDTO meta : metas) {
            Method m = meta.getMethod();
            invokerCache.computeIfAbsent(m, k -> createInvoker(m, meta.getBean()));
            argCache.computeIfAbsent(m, this::buildArgBuilders);
            warmReader(m);
        }
    }

    private void warmReader(Method method) {
        Class<?>[] types = method.getParameterTypes();
        Type[] generics = method.getGenericParameterTypes();

        for (int i = 0; i < types.length; i++) {

            Class<?> c = List.class.isAssignableFrom(types[i])
                ? extractGeneric(generics[i])
                : types[i];

            if (c == ConsumerRecord.class
                || c == Message.class
                || c == Acknowledgment.class) {
                continue;
            }

            readerCache.get(c, k -> JacksonUtil.getMapper().readerFor(k));
        }
    }

    private void registerAll(List<KafkaMultiListenerMetaDTO> metas) {
        Map<KafkaConsumerKeyDTO, List<KafkaMultiListenerMetaDTO>> grouped = group(metas);

        int count = 0;

        for (var entry : grouped.entrySet()) {
            KafkaMultiListenerMetaDTO meta = merge(entry.getValue());
            register(meta);

            count++;
        }

        log.info("Kafka listeners registered, sourceSize={}, listenerSize={}", metas.size(), count);
    }

    private void register(KafkaMultiListenerMetaDTO meta) {
        String listenerId = buildId(meta);
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
        AbstractMessageListenerContainer<String, Object> container =
            factory.createContainer(meta.getTopics());
        container.setBeanName(listenerId);
        ContainerProperties p = container.getContainerProperties();
        final String clientId = CharSequenceUtil.isNotBlank(p.getClientId())
            ? p.getClientId() : clusterManager.getConsumerClientId(meta.getCluster());

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
                "Kafka listener start failed, cluster={}, group={}, topics={}",
                meta.getCluster(),
                meta.getGroup(),
                meta.getTopics(),
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
        boolean manualAck = AckMode.MANUAL.equals(ackMode)
            || AckMode.MANUAL_IMMEDIATE.equals(ackMode);
        boolean hasAck = Arrays.stream(meta.getMethod().getParameterTypes())
            .anyMatch(Acknowledgment.class::isAssignableFrom);
        if (manualAck && !hasAck) {
            throw new IllegalStateException("Kafka listener AckMode.MANUAL requires Acknowledgment parameter, method=" + meta.getMethod());
        }

        // 批处理
        if (batch) {
            if (manualAck) {
                p.setMessageListener((BatchAcknowledgingMessageListener<String, Object>)
                    (records, ack) -> dispatch(clusterId, listenerId, clientId, meta, records, ack)
                );
            } else {
                p.setMessageListener((BatchMessageListener<String, Object>)
                    records -> dispatch(clusterId, listenerId, clientId, meta, records, null)
                );
            }
            return;
        } else {
            // 单处理
            if (manualAck) {
                p.setMessageListener((AcknowledgingMessageListener<String, Object>)
                    (record, ack) -> dispatch(clusterId, listenerId, clientId, meta, Collections.singletonList(record), ack)
                );
            } else {
                p.setMessageListener((MessageListener<String, Object>)
                    record -> dispatch(clusterId, listenerId, clientId, meta, Collections.singletonList(record), null)
                );
            }
        }
    }

    private void dispatch(
        String clusterId,
        String listenerId,
        String clientId,
        KafkaMultiListenerMetaDTO meta,
        List<ConsumerRecord<String, Object>> messages,
        Acknowledgment acknowledgment) {

        dispatcher.dispatch(
            new KafkaMessageHandler(clusterId, listenerId, clientId, meta.getCluster(), meta.getTopics(), meta.getGroup(),
                ignored -> invoke(meta, messages, acknowledgment)),
            messages);
    }

    private void invoke(
        KafkaMultiListenerMetaDTO meta,
        List<ConsumerRecord<String, Object>> records,
        Acknowledgment ack) {

        Method m = meta.getMethod();
        Object[] args = buildArgs(
            argCache.computeIfAbsent(m, this::buildArgBuilders),
            records,
            ack
        );

        try {
            invokerCache.computeIfAbsent(m, x -> createInvoker(m, meta.getBean()))
                .invoke(args);
        } catch (Throwable e) {
            log.error(
                "Kafka listener invoke failed, cluster={}, group={}, topics={}, method={}",
                meta.getCluster(),
                meta.getGroup(),
                meta.getTopics(),
                m,
                e
            );

            throw new RuntimeException(e);
        }
    }

    private Object[] buildArgs(
        ArgBuilder[] builders,
        List<?> records,
        Acknowledgment ack) {

        Object[] args = new Object[builders.length];
        for (int i = 0; i < builders.length; i++) {
            Object arg = builders[i].build(records);
            if (arg == null && builders[i] instanceof AckArgBuilder) {
                arg = ack;
            }

            args[i] = arg;
        }

        return args;
    }

    private ArgBuilder[] buildArgBuilders(Method m) {
        Class<?>[] t = m.getParameterTypes();
        Type[] g = m.getGenericParameterTypes();

        ArgBuilder[] arr = new ArgBuilder[t.length];

        for (int i = 0; i < t.length; i++) {
            arr[i] = createBuilder(t[i], g[i]);
        }

        return arr;
    }

    private ArgBuilder createBuilder(Class<?> type, Type generic) {
        if (Acknowledgment.class == type) {
            return new AckArgBuilder();
        }

        if (Message.class.isAssignableFrom(type)) {
            return records -> MessageBuilder.withPayload(extractValue(records.get(0))).build();
        }

        if (type == ConsumerRecord.class) {
            return records -> records.get(0);
        }

        if (List.class.isAssignableFrom(type)) {
            Class<?> clazz = extractGeneric(generic);

            if (clazz == ConsumerRecord.class) {
                return records -> records;
            }

            if (Message.class.isAssignableFrom(clazz)) {
                return records -> records.stream()
                    .map(x -> MessageBuilder.withPayload(extractValue(x)).build())
                    .toList();
            }

            ObjectReader reader = getReader(clazz);
            return records -> {
                List<Object> list = new ArrayList<>(records.size());
                for (Object x : records) {
                    list.add(read(extractValue(x), reader));
                }
                return list;
            };
        }

        ObjectReader reader = getReader(type);
        return records -> read(extractValue(records.get(0)), reader);
    }

    private Object extractValue(Object obj) {
        if (obj instanceof ConsumerRecord<?, ?> r) {
            return r.value();
        }

        return obj;
    }

    private Class<?> extractGeneric(Type type) {
        if (type instanceof ParameterizedType pt) {
            Type actual = pt.getActualTypeArguments()[0];
            if (actual instanceof Class<?> c) {
                return c;
            }

            if (actual instanceof ParameterizedType p
                && p.getRawType() instanceof Class<?> c) {
                return c;
            }
        }

        return Object.class;
    }

    private ObjectReader getReader(Class<?> clazz) {
        return readerCache.get(clazz,
            c -> JacksonUtil.getMapper().readerFor(c));
    }

    private Object read(Object value, ObjectReader reader) {
        try {
            if (value instanceof byte[] bytes) {
                return reader.readValue(bytes);
            }

            if (value instanceof String str) {
                return reader.readValue(str);
            }

            return value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private BeanInvoker createInvoker(Method m, Object bean) {
        try {
            MethodHandle handle = MethodHandles
                .privateLookupIn(bean.getClass(), MethodHandles.lookup())
                .unreflect(m)
                .bindTo(bean);

            return handle.asSpreader(Object[].class, m.getParameterCount())::invoke;

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private KafkaMultiListenerMetaDTO resolve(KafkaMultiListenerMetaDTO meta) {
        String cluster = CharSequenceUtil.isBlank(meta.getCluster())
            ? clusterManager.getDefaultCluster()
            : meta.getCluster();

        return KafkaMultiListenerMetaDTO.builder()
            .bean(meta.getBean())
            .method(meta.getMethod())
            .cluster(cluster)
            .topics(meta.getTopics())
            .group(meta.getGroup())
            .build();
    }

    private KafkaMultiListenerMetaDTO merge(List<KafkaMultiListenerMetaDTO> list) {
        KafkaMultiListenerMetaDTO first = list.get(0);

        String[] topics = list.stream()
            .flatMap(x -> Arrays.stream(x.getTopics()))
            .filter(CharSequenceUtil::isNotBlank)
            .distinct()
            .toArray(String[]::new);

        return KafkaMultiListenerMetaDTO.builder()
            .bean(first.getBean())
            .method(first.getMethod())
            .cluster(first.getCluster())
            .group(first.getGroup())
            .topics(topics)
            .build();
    }

    private Map<KafkaConsumerKeyDTO, List<KafkaMultiListenerMetaDTO>> group(List<KafkaMultiListenerMetaDTO> metas) {
        return metas
            .stream()
            .collect(Collectors.groupingBy(
                entity -> new KafkaConsumerKeyDTO(
                    entity.getCluster(),
                    entity.getGroup()
                )
            ));
    }

    private String buildId(KafkaMultiListenerMetaDTO meta) {
        String str = meta.getMethod().toGenericString()
            + SymbolConstant.WELL_NO
            + String.join(SymbolConstant.COMMA, meta.getTopics());

        return KafkaConstant.KAFKA
            + SymbolConstant.HORIZONTAL_LINE + meta.getCluster()
            + SymbolConstant.HORIZONTAL_LINE + meta.getGroup()
            + SymbolConstant.HORIZONTAL_LINE + SecureUtil.md5(str);
    }

    @FunctionalInterface
    private interface ArgBuilder {

        /**
         * 构建
         *
         * @param records 列表
         * @return 结果
         */
        Object build(List<?> records);
    }

    private static final class AckArgBuilder implements ArgBuilder {

        /**
         * 构建
         *
         * @param records 记录
         * @return 结果
         */
        @Override
        public Object build(List<?> records) {
            return null;
        }
    }

    @FunctionalInterface
    private interface BeanInvoker {

        /**
         * 调用
         *
         * @param args 参数
         * @return 结果
         * @throws Throwable 异常
         */
        Object invoke(Object[] args) throws Throwable;
    }
}