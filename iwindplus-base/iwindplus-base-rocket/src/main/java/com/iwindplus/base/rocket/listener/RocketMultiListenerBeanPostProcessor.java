/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.listener;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.rocket.domain.annotation.RocketMultiListener;
import com.iwindplus.base.rocket.domain.dto.RocketMultiListenerMetaDTO;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
public class RocketMultiListenerBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {

    private ConfigurableListableBeanFactory beanFactory;

    private final List<RocketMultiListenerMetaDTO> metadata = new CopyOnWriteArrayList<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();

        ReflectionUtils.doWithMethods(clazz, method -> {
            RocketMultiListener ann = method.getAnnotation(RocketMultiListener.class);
            if (ann == null) {
                return;
            }

            RocketMultiListenerMetaDTO meta = buildMetadata(bean, method, ann);
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
    public List<RocketMultiListenerMetaDTO> getMetadata() {
        final List<RocketMultiListenerMetaDTO> copy = new ArrayList<>(metadata);
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

    private RocketMultiListenerMetaDTO buildMetadata(Object bean, Method method, RocketMultiListener ann) {
        final String cluster = resolveCluster(ann.cluster(), beanFactory);
        final String topic = resolveTopic(ann.topic(), beanFactory);
        final String group = resolveGroup(ann.group(), beanFactory);

        return RocketMultiListenerMetaDTO
            .builder()
            .bean(bean)
            .method(method)
            .cluster(cluster)
            .topic(topic)
            .tag(ann.tag())
            .group(group)
            .orderly(ann.orderly())
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

    private String resolveTopic(String topic, ConfigurableListableBeanFactory beanFactory) {
        if (CharSequenceUtil.isBlank(topic)) {
            return null;
        }
        return String.valueOf(resolveExpression(topic, beanFactory));
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
