/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.kafka.domain.constant.KafkaConstant;
import com.iwindplus.base.kafka.domain.constant.KafkaConstant.BizRetryConstant;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty.KafkaBindingConfig;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty.KafkaConsumerConfig;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty.KafkaMultiClusterConfig;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty.KafkaProducerConfig;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka 动态 Topic 注册器.
 *
 * @author zengdegui
 * @since 2025/11/22 10:53
 */
@Slf4j
public final class KafkaDynamicRegistry {

    /**
     * 默认超时时间（秒）
     */
    private static final int DEFAULT_TIMEOUT_SEC = 10;

    /**
     * 创建 Topic（不存在时）
     *
     * @param clusterName   集群名称
     * @param clusterConfig 集群配置
     * @param adminClient   kafka admin client
     * @param timeoutSec    超时时间（秒）
     */
    public static void createTopicsIfAbsent(
        String clusterName,
        KafkaMultiClusterConfig clusterConfig,
        AdminClient adminClient,
        Integer timeoutSec) {

        if (clusterConfig == null || adminClient == null) {
            return;
        }

        KafkaConsumerConfig consumer = clusterConfig.getConsumer();
        if (consumer == null || CollUtil.isEmpty(consumer.getBindings())) {
            return;
        }

        int timeout = Optional.ofNullable(timeoutSec)
            .filter(v -> v > 0)
            .orElse(DEFAULT_TIMEOUT_SEC);

        try {
            List<KafkaBindingConfig> topics = buildAllTopics(clusterName, clusterConfig);

            if (CollUtil.isEmpty(topics)) {
                return;
            }

            Set<String> existingTopics =
                adminClient.listTopics()
                    .names()
                    .get(timeout, TimeUnit.SECONDS);

            List<NewTopic> newTopics = topics.stream()
                .filter(KafkaDynamicRegistry::isAutoCreate)
                .filter(topic -> !existingTopics.contains(topic.getTopic()))
                .map(KafkaDynamicRegistry::createTopic)
                .toList();

            if (CollUtil.isEmpty(newTopics)) {
                log.info("No new Kafka topics need to be created, cluster={}",
                    clusterName
                );

                return;
            }

            adminClient.createTopics(newTopics)
                .all()
                .get(timeout, TimeUnit.SECONDS);

            log.info(
                "Kafka topics created successfully, cluster={}, topics={}",
                clusterName,
                newTopics.stream().map(NewTopic::name).toList()
            );
        } catch (Exception e) {
            log.warn(
                "Kafka topic creation failed, cluster={}, error={}",
                clusterName,
                e.getMessage(),
                e
            );
        }
    }

    /**
     * 构建所有 Topic
     */
    private static List<KafkaBindingConfig> buildAllTopics(
        String clusterName,
        KafkaMultiClusterConfig clusterConfig) {

        KafkaConsumerConfig consumer = clusterConfig.getConsumer();

        List<KafkaBindingConfig> sourceBindings = new ArrayList<>(consumer.getBindings());

        List<KafkaBindingConfig> result = new ArrayList<>(sourceBindings);

        // default producer topic
        addDefaultProducerTopic(clusterConfig, result);

        // retry topics
        if (Boolean.TRUE.equals(consumer.getEnabledBizRetry())) {
            sourceBindings.stream()
                .map(binding -> toRetryTopics(binding))
                .forEach(result::addAll);
        }

        // dlq topics
        if (Boolean.TRUE.equals(consumer.getEnabledBizDlq())) {
            sourceBindings.stream()
                .map(KafkaDynamicRegistry::toDlqTopic)
                .forEach(result::add);
        }

        return deduplicate(result);
    }

    private static void addDefaultProducerTopic(
        KafkaMultiClusterConfig clusterConfig,
        List<KafkaBindingConfig> result) {

        KafkaProducerConfig producer = clusterConfig.getProducer();

        if (producer == null
            || CharSequenceUtil.isBlank(producer.getDefaultTopic())) {

            return;
        }

        KafkaBindingConfig binding = new KafkaBindingConfig();

        binding.setTopic(producer.getDefaultTopic());
        binding.setAutoCreate(true);

        result.add(binding);
    }

    private static List<KafkaBindingConfig> deduplicate(List<KafkaBindingConfig> bindings) {
        Map<String, KafkaBindingConfig> uniqueMap = new LinkedHashMap<>();

        for (KafkaBindingConfig binding : bindings) {
            if (binding == null || CharSequenceUtil.isBlank(binding.getTopic())) {
                continue;
            }

            uniqueMap.putIfAbsent(binding.getTopic(), binding);
        }
        return new ArrayList<>(uniqueMap.values());
    }

    public static NewTopic createTopic(KafkaBindingConfig config) {
        TopicBuilder builder = TopicBuilder.name(config.getTopic());

        if (config.getPartitions() > 0) {
            builder.partitions(config.getPartitions());
        }

        if (config.getReplicationFactor() > 0) {
            builder.replicas(config.getReplicationFactor());
        }

        if (config.getArguments() != null) {
            config.getArguments().forEach(builder::config);
        }

        return builder.build();
    }

    private static List<KafkaBindingConfig> toRetryTopics(KafkaBindingConfig origin) {
        List<KafkaBindingConfig> topics = new ArrayList<>(10);
        for (String suffix : KafkaConstant.KAFKA_RETRY_SUFFIXES) {
            KafkaBindingConfig retry = copy(origin);
            retry.setTopic(suffix);
            retry.setAutoCreate(true);

            topics.add(retry);
        }

        return topics;
    }

    private static KafkaBindingConfig toDlqTopic(
        KafkaBindingConfig origin) {

        KafkaBindingConfig dlq = copy(origin);

        dlq.setTopic(BizRetryConstant.KAFKA_DLQ);
        dlq.setAutoCreate(true);

        return dlq;
    }

    private static KafkaBindingConfig copy(
        KafkaBindingConfig source) {

        KafkaBindingConfig target = new KafkaBindingConfig();
        target.setPartitions(source.getPartitions());
        target.setReplicationFactor(source.getReplicationFactor());
        target.setArguments(source.getArguments());

        return target;
    }

    private static boolean isAutoCreate(
        KafkaBindingConfig binding) {
        return binding != null
            && Boolean.TRUE.equals(binding.getAutoCreate())
            && CharSequenceUtil.isNotBlank(binding.getTopic());
    }
}