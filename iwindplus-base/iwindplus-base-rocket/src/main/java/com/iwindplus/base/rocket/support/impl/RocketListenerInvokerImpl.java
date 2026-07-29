/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.support.impl;

import com.fasterxml.jackson.databind.ObjectReader;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.rocket.domain.dto.RocketMultiListenerMetaDTO;
import com.iwindplus.base.rocket.support.RocketListenerInvoker;
import com.iwindplus.base.util.JacksonUtil;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.DisposableBean;

/**
 * Rocket监听器调用器实现类.
 *
 * @author zengdegui
 * @since 2026/07/28
 */
@Slf4j
public class RocketListenerInvokerImpl implements RocketListenerInvoker, DisposableBean {

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

    /**
     * 预热缓存.
     *
     * @param metas listener元数据
     */
    @Override
    public void preWarm(List<RocketMultiListenerMetaDTO> metas) {
        metas.forEach(meta -> {
            Method method = meta.getMethod();
            invokerCache.get(method,
                m -> createInvoker(m, meta.getBean()));
            argCache.get(method, this::buildArgMetadata);
            warmReader(method);
        });

        log.info(
            "RocketListenerInvoker cache warm success,size={}",
            metas.size()
        );
    }

    /**
     * 调用监听方法.
     *
     * @param meta Rocket监听元数据
     * @param msgs 消息列表
     */
    @Override
    public void invoke(RocketMultiListenerMetaDTO meta, List<MessageExt> msgs) {
        Method method = meta.getMethod();

        ArgMetadata[] metadata =
            argCache.get(method, this::buildArgMetadata);

        Object[] args = new Object[metadata.length];
        for (int i = 0; i < metadata.length; i++) {
            args[i] = buildArg(metadata[i], msgs);
        }

        try {
            invokerCache.get(
                method,
                m -> createInvoker(m, meta.getBean())
            ).invoke(args);
        } catch (Throwable e) {
            log.error(
                "Rocket listener invoke failed, cluster={}, group={}, topic={}, method={}",
                meta.getCluster(),
                meta.getGroup(),
                meta.getTopic(),
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
            if (types[i] == MessageExt.class) {
                continue;
            }

            Class<?> c = List.class.isAssignableFrom(types[i])
                ? extractGeneric(generics[i])
                : types[i];

            getReader(c);
        }
    }

    private ArgMetadata[] buildArgMetadata(Method method) {
        Class<?>[] types =
            method.getParameterTypes();

        Type[] generics =
            method.getGenericParameterTypes();

        ArgMetadata[] r = new ArgMetadata[types.length];

        for (int i = 0; i < types.length; i++) {
            r[i] = buildArgMetadata(types[i], generics[i]);
        }

        return r;
    }

    private ArgMetadata buildArgMetadata(Class<?> t, Type g) {
        if (t == MessageExt.class) {
            return new ArgMetadata(ArgType.MSG, null);
        }

        if (List.class.isAssignableFrom(t)) {
            Class<?> c = extractGeneric(g);

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
    private Class<?> extractGeneric(Type t) {
        if (t instanceof ParameterizedType p) {
            return (Class<?>) p.getActualTypeArguments()[0];
        }
        throw new IllegalArgumentException("Unsupported generic type");
    }

    /**
     * JSON转换.
     */
    private Object read(MessageExt m, ObjectReader r) {
        try {
            return r.readValue(m.getBody());
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

    enum ArgType {MSG, MSG_LIST, DTO, DTO_LIST}

    record ArgMetadata(ArgType type, ObjectReader reader) {

    }

    @FunctionalInterface
    private interface BeanInvoker {

        Object invoke(Object[] args)
            throws Throwable;
    }
}