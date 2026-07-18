/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.listener;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.kafka.domain.annotation.KafkaMultiListener;
import com.iwindplus.base.kafka.domain.dto.KafkaMultiListenerMetaDTO;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ReflectionUtils;

/**
 * 核心 BeanPostProcessor（只收集，不注册）.
 *
 * @author zengdegui
 * @since 2026/03/26 00:32
 */
@Slf4j
@RequiredArgsConstructor
public class KafkaMultiListenerBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {

    private ConfigurableListableBeanFactory beanFactory;

    private final List<KafkaMultiListenerMetaDTO> metadata = new CopyOnWriteArrayList<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();

        ReflectionUtils.doWithMethods(clazz, method -> {
            KafkaMultiListener ann = method.getAnnotation(KafkaMultiListener.class);
            if (ann == null) {
                return;
            }

            KafkaMultiListenerMetaDTO meta = buildMetadata(bean, method, ann);
            metadata.add(meta);
        });

        return bean;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    /**
     * 获取监听元数据定义.
     *
     * @return 监听元数据定义
     */
    public List<KafkaMultiListenerMetaDTO> getMetadata() {
        final List<KafkaMultiListenerMetaDTO> copy = new ArrayList<>(metadata);
        metadata.clear();
        return copy;
    }

    /**
     * 获取 BeanFactory.
     *
     * @return BeanFactory
     */
    public ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    private KafkaMultiListenerMetaDTO buildMetadata(Object bean, Method method, KafkaMultiListener ann) {
        final String cluster = resolveCluster(ann.cluster(), beanFactory);
        final String[] topics = resolveTopics(ann.topics(), beanFactory);
        final String group = resolveGroup(ann.group(), beanFactory);

        return KafkaMultiListenerMetaDTO
            .builder()
            .bean(bean)
            .method(method)
            .cluster(cluster)
            .topics(topics)
            .group(group)
            .build();
    }

    private String resolveCluster(String cluster, ConfigurableListableBeanFactory beanFactory) {
        if (CharSequenceUtil.isBlank(cluster)) {
            return null;
        }
        return String.valueOf(resolveExpression(cluster, beanFactory));
    }

    private String resolveGroup(String group, ConfigurableListableBeanFactory beanFactory) {
        if (CharSequenceUtil.isBlank(group)) {
            return null;
        }
        return String.valueOf(resolveExpression(group, beanFactory));
    }

    private String[] resolveTopics(String[] topics, ConfigurableListableBeanFactory beanFactory) {
        return Arrays.stream(topics)
            .map(topic -> this.resolveExpression(topic, beanFactory))
            .map(Object::toString)
            .flatMap(v -> Arrays.stream(v.split(",")))
            .toArray(String[]::new);
    }

    private Object resolveExpression(String value, ConfigurableListableBeanFactory beanFactory) {
        if (CharSequenceUtil.isBlank(value)) {
            return value;
        }

        // 先解析 ${}
        String resolved = beanFactory.resolveEmbeddedValue(value);

        // 再解析 #{}
        BeanExpressionResolver resolver = beanFactory.getBeanExpressionResolver();
        if (resolver == null) {
            return resolved;
        }

        return resolver.evaluate(resolved, new BeanExpressionContext(beanFactory, null));
    }
}
