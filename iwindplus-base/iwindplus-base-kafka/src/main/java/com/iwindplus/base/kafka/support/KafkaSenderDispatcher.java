/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support;

import com.iwindplus.base.kafka.domain.dto.KafkaMessageDTO;
import java.util.Map;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Kafka发送调度器.
 *
 * @author zengdegui
 * @since 2026/05/08 16:36
 */
public interface KafkaSenderDispatcher {

    /**
     * 发送
     *
     * @param cluster  集群名称
     * @param topic    主题名称
     * @param key      key
     * @param headers  头
     * @param message  消息体
     * @param executor 执行器
     * @param <T>      泛型
     * @return T
     */
    <T> T dispatch(
        String cluster,
        String topic,
        String key,
        Map<String, Object> headers,
        String message,
        KafkaSendExecutor<T> executor);

    /**
     * 对外同步 Kafka 执行器
     */
    @FunctionalInterface
    interface KafkaSendExecutor<T> extends ExecutorWrapper<T, KafkaTemplate<String, Object>> {

    }

    /**
     * 抽象执行器接口
     */
    @FunctionalInterface
    interface ExecutorWrapper<T, P> {

        /**
         * 执行
         *
         * @param producer 模板
         * @param message  消息
         * @return 结果
         */
        T execute(P producer, KafkaMessageDTO message);
    }

    /**
     * 抽象模板提供者
     */
    @FunctionalInterface
    interface TemplateProvider<P> {

        /**
         * 获取模板
         *
         * @param cluster 集群名称
         * @return 模板
         */
        P getTemplate(String cluster);
    }
}
