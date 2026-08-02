/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.SecureUtil;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.kafka.domain.constant.KafkaConstant;
import com.iwindplus.base.kafka.domain.dto.KafkaConsumerKeyDTO;
import com.iwindplus.base.kafka.domain.dto.KafkaMultiListenerMetaDTO;
import com.iwindplus.base.kafka.support.KafkaListenerInvoker;
import com.iwindplus.base.util.JacksonUtil;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * Kafka监听器调用器实现类.
 *
 * @author zengdegui
 * @since 2026/07/28
 */
@Slf4j
public class KafkaListenerInvokerImpl implements KafkaListenerInvoker, DisposableBean {

    /**
     * 方法参数解析缓存. Method -> ArgBuilder[]
     */
    private final Cache<Method, ArgBuilder[]> argCache =
        Caffeine.newBuilder()
            .maximumSize(2048)
            .build();

    /**
     * 方法调用缓存. Method -> BeanInvoker
     */
    private final Cache<Method, BeanInvoker> invokerCache =
        Caffeine.newBuilder()
            .maximumSize(2048)
            .build();

    /**
     * Jackson转换缓存. Class -> ObjectReader
     */
    private final Cache<Class<?>, ObjectReader> readerCache =
        Caffeine.newBuilder()
            .maximumSize(2048)
            .build();

    /**
     * 监听器元数据缓存. listenerId -> KafkaMultiListenerMetaDTO
     */
    private final Cache<String, KafkaMultiListenerMetaDTO> metaCache =
        Caffeine.newBuilder()
            .maximumSize(2048)
            .build();

    @Override
    public List<KafkaMultiListenerMetaDTO> listGroupMergePreWarm(List<KafkaMultiListenerMetaDTO> metas) {
        if (CollUtil.isEmpty(metas)) {
            return Collections.emptyList();
        }

        final List<KafkaMultiListenerMetaDTO> list = listGroupMergeData(metas);
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        preWarmData(list);

        return list;
    }

    /**
     * 根据listenerId获取监听器元数据.
     *
     * @param listenerId listenerId
     * @return 监听器元数据，不存在时返回null
     */
    @Override
    public KafkaMultiListenerMetaDTO getMeta(String listenerId) {
        return metaCache.getIfPresent(listenerId);
    }

    /**
     * 调用监听方法.
     *
     * @param meta     kafka监听元数据
     * @param records  消息列表
     * @param ack      ack对象
     * @param consumer kafka consumer
     */
    @Override
    public void invoke(
        KafkaMultiListenerMetaDTO meta,
        List<ConsumerRecord<String, Object>> records,
        Acknowledgment ack,
        Consumer<?, ?> consumer) {

        Method method = meta.getMethod();

        ArgBuilder[] builders =
            argCache.get(method, this::buildArgBuilders);

        Object[] args =
            buildArgs(builders, records, ack, consumer);

        try {
            invokerCache.get(
                method,
                m -> createInvoker(m, meta.getBean())
            ).invoke(args);
        } catch (Throwable e) {
            log.error(
                "Kafka listener invoke failed,cluster={},group={},topics={},method={}",
                meta.getCluster(),
                meta.getGroup(),
                meta.getTopics(),
                method,
                e
            );

            throw new RuntimeException(e);
        }
    }

    /**
     * 构建方法参数.
     */
    private Object[] buildArgs(
        ArgBuilder[] builders,
        List<?> records,
        Acknowledgment ack,
        Consumer<?, ?> consumer) {

        Object[] args = new Object[builders.length];

        for (int i = 0; i < builders.length; i++) {
            args[i] = builders[i].build(
                records,
                ack,
                consumer
            );
        }

        return args;
    }

    /**
     * 构建参数解析器.
     */
    private ArgBuilder[] buildArgBuilders(Method method) {
        Class<?>[] types = method.getParameterTypes();
        Type[] genericTypes = method.getGenericParameterTypes();

        ArgBuilder[] builders =
            new ArgBuilder[types.length];

        for (int i = 0; i < types.length; i++) {
            builders[i] =
                createBuilder(
                    types[i],
                    genericTypes[i]
                );
        }

        return builders;
    }

    /**
     * 创建参数解析器.
     */

    private ArgBuilder createBuilder(
        Class<?> type,
        Type generic) {

        if (Acknowledgment.class.isAssignableFrom(type)) {
            return (records, ack, consumer) -> {
                if (ack == null) {
                    throw new IllegalStateException(
                        "Acknowledgment is required"
                    );
                }
                return ack;
            };
        }

        if (Consumer.class.isAssignableFrom(type)) {
            return (records, ack, consumer) -> {
                if (consumer == null) {
                    throw new IllegalStateException(
                        "Consumer is required"
                    );
                }
                return consumer;
            };
        }

        if (ConsumerRecord.class.isAssignableFrom(type)) {
            return (records, ack, consumer) ->
                records.get(0);
        }

        if (Message.class.isAssignableFrom(type)) {
            return (records, ack, consumer) ->
                MessageBuilder
                    .withPayload(
                        extractValue(records.get(0))
                    )
                    .build();
        }

        if (List.class.isAssignableFrom(type)) {
            return buildListBuilder(generic);
        }

        return buildObjectBuilder(type);
    }

    private ArgBuilder buildObjectBuilder(Class<?> type) {
        ObjectReader reader = getReader(type);

        return (records, ack, consumer) ->
            read(
                extractValue(records.get(0)),
                reader
            );
    }

    private ArgBuilder buildListBuilder(Type generic) {
        Class<?> genericClass = extractGeneric(generic);

        if (ConsumerRecord.class.isAssignableFrom(genericClass)) {
            return (records, ack, consumer) ->
                records;
        }

        if (Message.class.isAssignableFrom(genericClass)) {
            return (records, ack, consumer) ->
                records.stream()
                    .map(record ->
                        MessageBuilder
                            .withPayload(
                                extractValue(record)
                            )
                            .build()
                    )
                    .toList();
        }

        ObjectReader reader =
            getReader(genericClass);

        return (records, ack, consumer) -> {

            List<Object> result =
                new ArrayList<>(records.size());

            for (Object record : records) {
                result.add(
                    read(
                        extractValue(record),
                        reader
                    )
                );
            }

            return result;
        };
    }

    /**
     * 预加载Reader.
     */
    private void warmReader(Method method) {

        Class<?>[] types =
            method.getParameterTypes();

        Type[] generics =
            method.getGenericParameterTypes();

        for (int i = 0; i < types.length; i++) {

            Class<?> clazz =
                List.class.isAssignableFrom(types[i])
                    ? extractGeneric(generics[i])
                    : types[i];

            if (isFrameworkType(clazz)) {
                continue;
            }

            getReader(clazz);
        }
    }

    /**
     * 判断是否Kafka框架参数.
     */
    private boolean isFrameworkType(Class<?> clazz) {
        return ConsumerRecord.class.isAssignableFrom(clazz)
            || Message.class.isAssignableFrom(clazz)
            || Acknowledgment.class.isAssignableFrom(clazz)
            || Consumer.class.isAssignableFrom(clazz);
    }

    /**
     * 获取Reader.
     */
    private ObjectReader getReader(Class<?> clazz) {
        return readerCache.get(
            clazz,
            c -> JacksonUtil
                .getMapper()
                .readerFor(c)
        );
    }

    /**
     * 提取消息value.
     */
    private Object extractValue(Object obj) {
        return obj instanceof ConsumerRecord<?, ?> record
            ? record.value()
            : obj;
    }

    /**
     * 泛型解析.
     */
    private Class<?> extractGeneric(Type type) {

        if (type instanceof ParameterizedType pt) {

            Type actual =
                pt.getActualTypeArguments()[0];

            if (actual instanceof Class<?> clazz) {
                return clazz;
            }

            if (actual instanceof ParameterizedType p
                && p.getRawType() instanceof Class<?> clazz) {
                return clazz;
            }
        }

        return Object.class;
    }

    /**
     * JSON转换.
     */
    private Object read(
        Object value,
        ObjectReader reader) {

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

    /**
     * 创建方法调用器.
     */
    private BeanInvoker createInvoker(
        Method method,
        Object bean) {

        try {

            method.setAccessible(true);

            MethodHandle handle =
                MethodHandles.lookup()
                    .unreflect(method)
                    .bindTo(bean);

            MethodHandle spreader =
                handle.asSpreader(
                    Object[].class,
                    method.getParameterCount()
                );

            return args ->
                spreader.invoke(args);

        } catch (Throwable e) {

            throw new RuntimeException(
                "Create invoker failed:" + method,
                e
            );
        }
    }

    /**
     * 销毁.
     */
    @Override
    public void destroy() {

        argCache.invalidateAll();
        invokerCache.invalidateAll();
        readerCache.invalidateAll();
    }

    @FunctionalInterface
    private interface ArgBuilder {

        Object build(
            List<?> records,
            Acknowledgment ack,
            Consumer<?, ?> consumer
        );
    }

    @FunctionalInterface
    private interface BeanInvoker {

        Object invoke(Object[] args)
            throws Throwable;
    }

    private void preWarmData(List<KafkaMultiListenerMetaDTO> list) {
        Set<Method> warmedReaders = new HashSet<>();

        list.stream().forEach(meta -> {
            Method method = meta.getMethod();

            invokerCache.get(
                method,
                m -> createInvoker(
                    m,
                    meta.getBean()
                )
            );

            argCache.get(
                method,
                this::buildArgBuilders
            );

            //  reader只需要预热一次
            if (warmedReaders.add(method)) {
                warmReader(method);
            }

            // 缓存元数据，使用listenerId作为key
            metaCache.put(meta.getListenerId(), meta);
        });
    }

    private List<KafkaMultiListenerMetaDTO> listGroupMergeData(List<KafkaMultiListenerMetaDTO> entities) {
        return entities.stream()
            .collect(
                Collectors.groupingBy(
                    entity ->
                        new KafkaConsumerKeyDTO(
                            entity.getCluster(),
                            entity.getGroup()
                        )
                )
            )
            .values()
            .stream()
            .filter(CollUtil::isNotEmpty)
            .map(metas -> {
                String[] topics =
                    metas.stream()
                        .flatMap(meta ->
                            Arrays.stream(
                                Optional.ofNullable(meta.getTopics())
                                    .orElse(new String[0])
                            )
                        )
                        .filter(CharSequenceUtil::isNotBlank)
                        .distinct()
                        .toArray(String[]::new);

                KafkaMultiListenerMetaDTO first =
                    metas.get(0);

                KafkaMultiListenerMetaDTO data =
                    KafkaMultiListenerMetaDTO.builder()
                        .bean(first.getBean())
                        .method(first.getMethod())
                        .cluster(first.getCluster())
                        .group(first.getGroup())
                        .topics(topics)
                        .build();

                data.setListenerId(buildId(data));
                return data;
            }).toList();
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
}