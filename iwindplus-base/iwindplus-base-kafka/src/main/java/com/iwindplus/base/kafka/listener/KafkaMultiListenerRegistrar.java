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
import com.iwindplus.base.kafka.domain.dto.KafkaConsumerInfoDTO;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
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

    private final ApplicationContext applicationContext;
    private final KafkaMultiListenerBeanPostProcessor bpp;
    private final KafkaClusterManager clusterManager;
    private final KafkaReceiverDispatcher dispatcher;

    private final Map<Method, BeanInvoker> invokerCache = new ConcurrentHashMap<>(16);
    private final Map<Method, ArgBuilder[]> argCache = new ConcurrentHashMap<>(16);
    private final Cache<Class<?>, ObjectReader> readerCache = Caffeine.newBuilder().maximumSize(1024).build();
    private final Map<String, KafkaConsumerInfoDTO> containerMap = new ConcurrentHashMap<>(16);

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
                c.getContainer().stop();
                c.getContainer().destroy();
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

    /**
     * 获取所有监听器.
     *
     * @return Map<String, KafkaConsumerInfoDTO>
     */
    public Map<String, KafkaConsumerInfoDTO> getContainerMap() {
        return containerMap;
    }

    /**
     * 按集群 + 消费组分组.
     *
     * @return Map<KafkaConsumerKeyDTO, List < KafkaConsumerInfoDTO>>
     */
    public Map<KafkaConsumerKeyDTO, List<KafkaConsumerInfoDTO>> groupByClusterAndGroup() {
        return containerMap
            .values()
            .stream()
            .collect(Collectors.groupingBy(
                entity -> new KafkaConsumerKeyDTO(
                    entity.getCluster(),
                    entity.getGroup()
                )
            ));
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

        final KafkaConsumerInfoDTO consumerInfo = KafkaConsumerInfoDTO
            .builder()
            .cluster(meta.getCluster())
            .group(meta.getGroup())
            .topics(new HashSet<>(Arrays.asList(meta.getTopics())))
            .listenerId(listenerId)
            .clientId(clientId)
            .maxConcurrency(property.getMaxConcurrency(meta.getCluster()))
            .container(container)
            .build();

        registerListener(clusterId, listenerId, clientId, meta, property, p);

        try {
            container.start();

            containerMap.put(listenerId, consumerInfo);

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

        // 投递新的消息
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
                records -> invoke(meta, records, acknowledgment, consumer),
                // 业务成功
                records -> {
                    // 开启了 containerProperties.setAsyncAcks(true);
                    // 虽然还是disruptor调用，但是 Spring Kafka 会缓存 ack，并由 Consumer线程最终提交
                    if (acknowledgment != null) {
                        acknowledgment.acknowledge();
                    }
                }
            ),
            consumer
        );
    }

    private void invoke(
        KafkaMultiListenerMetaDTO meta,
        List<ConsumerRecord<String, Object>> records,
        Acknowledgment ack,
        Consumer<?, ?> consumer) {

        Method m = meta.getMethod();
        Object[] args = buildArgs(
            argCache.computeIfAbsent(m, this::buildArgBuilders),
            records,
            ack,
            consumer
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
        Acknowledgment ack,
        Consumer<?, ?> consumer) {

        Object[] args = new Object[builders.length];

        for (int i = 0; i < builders.length; i++) {
            args[i] = builders[i].build(records, ack, consumer);
        }

        return args;
    }

    private ArgBuilder[] buildArgBuilders(Method method) {
        Class<?>[] types = method.getParameterTypes();
        Type[] genericTypes = method.getGenericParameterTypes();
        ArgBuilder[] builders = new ArgBuilder[types.length];

        for (int i = 0; i < types.length; i++) {
            builders[i] = createBuilder(types[i], genericTypes[i]);
        }

        return builders;
    }

    private ArgBuilder createBuilder(Class<?> type, Type generic) {
        if (Acknowledgment.class.isAssignableFrom(type)) {
            return (records, ack, consumer) -> ack;
        }
        if (Consumer.class.isAssignableFrom(type)) {
            return (records, ack, consumer) -> consumer;
        }
        if (ConsumerRecord.class.isAssignableFrom(type)) {
            return (records, ack, consumer) -> records.get(0);
        }
        if (Message.class.isAssignableFrom(type)) {
            return (records, ack, consumer) ->
                MessageBuilder
                    .withPayload(extractValue(records.get(0)))
                    .build();
        }
        if (List.class.isAssignableFrom(type)) {
            Class<?> genericClass = extractGeneric(generic);
            if (ConsumerRecord.class.isAssignableFrom(genericClass)) {
                return (records, ack, consumer) -> records;
            }

            if (Message.class.isAssignableFrom(genericClass)) {
                return (records, ack, consumer) ->
                    records.stream()
                        .map(record ->
                            MessageBuilder
                                .withPayload(extractValue(record))
                                .build()
                        ).toList();
            }

            ObjectReader reader = getReader(genericClass);

            return (records, ack, consumer) -> {
                List<Object> result = new ArrayList<>(records.size());
                for (Object record : records) {
                    result.add(read(extractValue(record), reader));
                }
                return result;
            };
        }

        ObjectReader reader = getReader(type);

        return (records, ack, consumer) -> read(extractValue(records.get(0)), reader);
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

            if (actual instanceof Class<?> clazz) {
                return clazz;
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

        return KafkaMultiListenerMetaDTO
            .builder()
            .bean(meta.getBean())
            .method(meta.getMethod())
            .cluster(cluster)
            .topics(meta.getTopics())
            .group(meta.getGroup())
            .build();
    }

    private KafkaMultiListenerMetaDTO merge(List<KafkaMultiListenerMetaDTO> list) {
        KafkaMultiListenerMetaDTO first = list.get(0);

        String[] topics =
            list.stream()
                .flatMap(x -> Arrays.stream(x.getTopics()))
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .toArray(String[]::new);

        return KafkaMultiListenerMetaDTO
            .builder()
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
         * 构建方法参数.
         *
         * @param records  消息列表
         * @param ack      ack
         * @param consumer kafka consumer
         * @return 方法参数
         */
        Object build(
            List<?> records,
            Acknowledgment ack,
            Consumer<?, ?> consumer
        );
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