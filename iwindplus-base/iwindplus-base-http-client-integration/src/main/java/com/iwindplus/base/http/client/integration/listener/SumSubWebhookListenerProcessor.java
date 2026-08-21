/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.listener;

import com.iwindplus.base.http.client.integration.annotation.SumSubWebhookListener;
import com.iwindplus.base.http.client.integration.domain.dto.sumsub.SumSubWebhookDTO;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

/**
 * SumSub Webhook监听器注解处理器.
 * <p>
 * 自动扫描所有带有@SumSubWebhookListener注解的方法，并将其注册为Webhook处理器。
 * </p>
 *
 * @author zengdegui
 * @since 2026/08/21
 */
@Slf4j
public class SumSubWebhookListenerProcessor implements BeanPostProcessor {

    private final Map<String, Consumer<SumSubWebhookDTO>> handlerMap = new ConcurrentHashMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);

        // 查找所有带有@SumSubWebhookListener注解的方法
        Set<Method> annotatedMethods = MethodIntrospector.selectMethods(
            targetClass,
            (ReflectionUtils.MethodFilter) method -> AnnotationUtils.findAnnotation(method, SumSubWebhookListener.class) != null
        );

        if (annotatedMethods.isEmpty()) {
            return bean;
        }

        // 为每个注解方法创建处理器
        annotatedMethods.forEach(method -> {
            SumSubWebhookListener annotation = AnnotationUtils.findAnnotation(method, SumSubWebhookListener.class);
            if (annotation == null) {
                return;
            }

            String webhookType = annotation.value();

            // 验证方法参数
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1 || !SumSubWebhookDTO.class.isAssignableFrom(parameterTypes[0])) {
                log.error("Method {} annotated with @SumSubWebhookListener must have exactly one parameter of type SumSubWebhookDTO",
                    method.getName());
                return;
            }

            // 设置方法可访问
            ReflectionUtils.makeAccessible(method);

            // 创建处理器
            Consumer<SumSubWebhookDTO> handler = webhookData -> {
                try {
                    method.invoke(bean, webhookData);
                } catch (Exception e) {
                    log.error("Failed to invoke @SumSubWebhookListener method: {}", method.getName(), e);
                    throw new RuntimeException("Failed to invoke webhook listener method", e);
                }
            };

            // 注册处理器
            Consumer<SumSubWebhookDTO> existingHandler = handlerMap.putIfAbsent(webhookType, handler);
            if (existingHandler != null) {
                log.warn("Duplicate @SumSubWebhookListener for type {}, existing handler will be replaced", webhookType);
                handlerMap.put(webhookType, handler);
            }

            log.info("Registered @SumSubWebhookListener: bean={}, method={}, type={}",
                beanName, method.getName(), webhookType);
        });

        return bean;
    }

    /**
     * 获取所有处理器.
     *
     * @return Map<String, Consumer<SumSubWebhookDTO>>
     */
    public Map<String, Consumer<SumSubWebhookDTO>> getHandlers() {
        return handlerMap;
    }
}
