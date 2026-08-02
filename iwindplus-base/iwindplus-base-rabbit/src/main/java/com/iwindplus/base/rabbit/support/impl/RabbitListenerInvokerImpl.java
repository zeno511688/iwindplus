/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.support.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.SecureUtil;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.rabbit.domain.constant.RabbitConstant;
import com.iwindplus.base.rabbit.domain.dto.RabbitConsumerKeyDTO;
import com.iwindplus.base.rabbit.domain.dto.RabbitMultiListenerMetaDTO;
import com.iwindplus.base.rabbit.support.RabbitListenerInvoker;
import com.iwindplus.base.util.JacksonUtil;
import com.rabbitmq.client.Channel;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.DisposableBean;

/**
 * Rabbit监听器调用器实现类.
 *
 * @author zengdegui
 * @since 2026/07/28
 */
@Slf4j
public class RabbitListenerInvokerImpl implements RabbitListenerInvoker, DisposableBean {

    /**
     * 方法参数解析缓存. Method -> ArgMetadata[]
     */
    private final Cache<Method, ArgMetadata[]> argCache =
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

    @Override
    public List<RabbitMultiListenerMetaDTO> listGroupMergePreWarm(List<RabbitMultiListenerMetaDTO> metas) {
        if (CollUtil.isEmpty(metas)) {
            return Collections.emptyList();
        }

        final List<RabbitMultiListenerMetaDTO> list = listGroupMergeData(metas);
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        preWarmData(list);

        return list;
    }

    @Override
    public void invoke(RabbitMultiListenerMetaDTO meta, List<Message> messages, Channel channel) {
        Method method = meta.getMethod();
        ArgMetadata[] metadata =
            argCache.get(method, this::buildArgMetadata);

        Object[] args = new Object[metadata.length];
        for (int i = 0; i < metadata.length; i++) {
            args[i] = buildArg(metadata[i], messages, channel);
        }
        try {
            invokerCache.get(
                method,
                m -> createInvoker(m, meta.getBean())
            ).invoke(args);
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

    /**
     * 预加载Reader.
     */
    private void warmReader(Method method) {

        Class<?>[] types =
            method.getParameterTypes();

        Type[] generics =
            method.getGenericParameterTypes();

        for (int i = 0; i < types.length; i++) {
            if (types[i] == Message.class || types[i] == Channel.class) {
                continue;
            }

            Class<?> clazz =
                List.class.isAssignableFrom(types[i])
                    ? extractGeneric(generics[i])
                    : types[i];

            getReader(clazz);
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
     * 泛型解析.
     */
    private Class<?> extractGeneric(Type type) {
        if (type instanceof ParameterizedType pt) {
            return (Class<?>) pt.getActualTypeArguments()[0];
        }
        throw new IllegalArgumentException("Unsupported generic type");
    }

    /**
     * JSON转换.
     */
    private Object read(Message message, ObjectReader reader) {
        try {
            return reader.readValue(message.getBody());
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

    enum ArgType {MSG, MSG_LIST, DTO, DTO_LIST, CHANNEL}

    record ArgMetadata(ArgType type, ObjectReader reader) {

    }

    @FunctionalInterface
    private interface BeanInvoker {

        Object invoke(Object[] args)
            throws Throwable;
    }

    private void preWarmData(List<RabbitMultiListenerMetaDTO> list) {
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
                this::buildArgMetadata
            );

            //  reader只需要预热一次
            if (warmedReaders.add(method)) {
                warmReader(method);
            }
        });
    }

    private List<RabbitMultiListenerMetaDTO> listGroupMergeData(List<RabbitMultiListenerMetaDTO> entities) {
        return entities.stream()
            .collect(
                Collectors.groupingBy(
                    entity ->
                        new RabbitConsumerKeyDTO(
                            entity.getCluster(),
                            entity.getGroup()
                        )
                )
            )
            .values()
            .stream()
            .filter(CollUtil::isNotEmpty)
            .map(metas -> {
                String[] queues =
                    metas.stream()
                        .flatMap(meta ->
                            Arrays.stream(
                                Optional.ofNullable(meta.getQueues())
                                    .orElse(new String[0])
                            )
                        )
                        .filter(CharSequenceUtil::isNotBlank)
                        .distinct()
                        .toArray(String[]::new);

                RabbitMultiListenerMetaDTO first =
                    metas.get(0);

                RabbitMultiListenerMetaDTO data =
                    RabbitMultiListenerMetaDTO.builder()
                        .bean(first.getBean())
                        .method(first.getMethod())
                        .cluster(first.getCluster())
                        .group(first.getGroup())
                        .queues(queues)
                        .build();

                data.setListenerId(buildId(data));
                return data;
            }).toList();
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