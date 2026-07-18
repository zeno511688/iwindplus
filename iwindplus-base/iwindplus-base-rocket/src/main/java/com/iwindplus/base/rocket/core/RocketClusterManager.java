/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.core;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.rocket.domain.dto.RocketConsumerKeyDTO;
import com.iwindplus.base.rocket.domain.enums.RocketCodeEnum;
import com.iwindplus.base.rocket.domain.property.RocketMultiProperty;
import com.iwindplus.base.rocket.domain.property.RocketMultiProperty.RocketMultiClusterConfig;
import com.iwindplus.base.rocket.domain.property.RocketMultiProperty.RocketProducerConfig;
import io.micrometer.observation.ObservationRegistry;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;

/**
 * Rocket集群管理器.
 *
 * @author zengdegui
 * @since 2026/03/21 22:01
 */
@Slf4j
@RequiredArgsConstructor
public class RocketClusterManager implements SmartLifecycle, DisposableBean {

    private final RocketMultiProperty property;
    private final ObservationRegistry observationRegistry;

    private final Map<String, DefaultMQProducer> producerMap = new ConcurrentHashMap<>(16);
    private final Map<RocketConsumerKeyDTO, DefaultMQPushConsumer> consumerMap = new ConcurrentHashMap<>(16);

    private volatile boolean running = false;

    @Override
    public void start() {
        this.initClusters();
        running = true;
    }

    @Override
    public void stop() {
        running = false;

        producerMap.forEach((cluster, producer) -> {
            try {
                producer.shutdown();
                log.info("Rocket producer destroyed: {}", cluster);
            } catch (Exception e) {
                log.error("Destroy Rocket producer error: {}", cluster, e);
            }
        });

        consumerMap.forEach((key, consumer) -> {
            try {
                consumer.shutdown();
                log.info("Rocket consumer destroyed: {}", key);
            } catch (Exception e) {
                log.error("Destroy Rocket consumer error: {}", key, e);
            }
        });
    }

    @Override
    public int getPhase() {
        // 最早启动
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * 销毁
     */
    @Override
    public void destroy() {
        stop();

        producerMap.clear();
        consumerMap.clear();
    }

    /**
     * 初始化所有集群.
     */
    public void initClusters() {
        if (Boolean.FALSE.equals(property.getEnabled())) {
            log.warn("Rocket multi disabled");
            return;
        }

        final Map<String, RocketMultiClusterConfig> clusters = property.getClusters();
        if (MapUtil.isEmpty(clusters)) {
            throw new BizException(RocketCodeEnum.ROCKET_CLUSTER_NOT_EXIST);
        }

        property.getClusters().forEach((clusterName, clusterConfig) -> {
            log.info("Initializing Rocket cluster: {}", clusterName);
            // 创建Producer
            buildProducer(clusterName, clusterConfig);
        });
    }

    /**
     * 获取ObservationRegistry.
     *
     * @return ObservationRegistry
     */
    public ObservationRegistry getObservationRegistry() {
        return observationRegistry;
    }

    /**
     * 获取RocketMultiProperty.
     *
     * @return RocketMultiProperty
     */
    public RocketMultiProperty getProperty() {
        return property;
    }

    /**
     * 获取默认集群.
     *
     * @return String
     */
    public String getDefaultCluster() {
        return property.getDefaultCluster();
    }

    /**
     * 获取消费组.
     *
     * @param cluster 集群名称
     * @param group   组
     * @return String
     */
    public String getGroup(String cluster, String group) {
        return property.getGroup(cluster, group);
    }

    /**
     * 获取生产者.
     *
     * @param cluster 集群名称
     * @return DefaultMQProducer
     */
    public DefaultMQProducer getProducer(String cluster) {
        return getOrThrow(
            producerMap,
            resolveCluster(cluster),
            RocketCodeEnum.ROCKET_CLUSTER_NOT_EXIST
        );
    }

    /**
     * 获取默认生产者.
     *
     * @return DefaultMQProducer
     */
    public DefaultMQProducer getDefaultProducer() {
        return getProducer(null);
    }

    /**
     * 获取消费者.
     *
     * @param cluster 集群名称
     * @param group   分组
     * @return DefaultMQPushConsumer
     */
    public DefaultMQPushConsumer getConsumer(String cluster, String group) {
        final RocketConsumerKeyDTO key = new RocketConsumerKeyDTO(cluster, group);
        return consumerMap.computeIfAbsent(key, k -> createConsumer(cluster, group));
    }

    private String resolveCluster(String cluster) {
        return CharSequenceUtil.isBlank(cluster) ? property.getDefaultCluster() : cluster;
    }

    private void buildProducer(String clusterName, RocketMultiClusterConfig clusterConfig) {
        final RocketProducerConfig producerConfig = clusterConfig.getProducer();
        if (Objects.isNull(producerConfig) || Boolean.FALSE.equals(producerConfig.getEnabled())) {
            return;
        }

        producerMap.computeIfAbsent(clusterName, key -> this.createProducer(clusterName));
    }

    private DefaultMQProducer createProducer(String clusterName) {
        try {
            final RocketMultiClusterConfig clusterConfig = property.getClusters().get(clusterName);
            final String producerGroup = property.getProducerGroup(clusterName);
            final RocketProducerConfig producerConfig = clusterConfig.getProducer();

            final DefaultMQProducer producer = new DefaultMQProducer(producerGroup);
            producer.setNamesrvAddr(clusterConfig.getNameServer());
            producer.setSendMsgTimeout(producerConfig.getSendMsgTimeout());
            producer.setRetryTimesWhenSendFailed(producerConfig.getRetryTimesWhenSendFailed());
            // 异步重试
            producer.setRetryTimesWhenSendAsyncFailed(
                Optional.ofNullable(producerConfig.getRetryTimesWhenSendAsyncFailed()).orElse(3)
            );
            // 是否在broker失败时重试其他broker
            producer.setRetryAnotherBrokerWhenNotStoreOK(
                Optional.ofNullable(producerConfig.getRetryAnotherBrokerWhenNotStoreOk()).orElse(true)
            );
            final DtpExecutor executor = getExecutor(clusterName, producerConfig.getThreadPoolName());
            if (executor != null) {
                producer.setAsyncSenderExecutor(executor);
            }

            producer.start();

            log.info("Create Producer for cluster: {})", clusterName);

            return producer;
        } catch (Exception ex) {
            log.error("Create producer failed, cluster={}", clusterName, ex);
            throw new RuntimeException("Create producer failed, cluster=" + clusterName, ex);
        }
    }

    public DefaultMQPushConsumer createConsumer(String clusterName, String group) {
        try {
            final RocketMultiClusterConfig clusterConfig =
                property.getClusters().get(resolveCluster(clusterName));

            final String consumerGroup = property.getGroup(clusterName, group);
            final RocketMultiProperty.RocketConsumerConfig consumerConfig = clusterConfig.getConsumer();

            final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
            consumer.setNamesrvAddr(clusterConfig.getNameServer());
            consumer.setMessageModel(consumerConfig.getMessageModel());
            consumer.setConsumeFromWhere(consumerConfig.getConsumeFromWhere());
            consumer.setConsumeTimeout(consumerConfig.getConsumeTimeout());
            consumer.setConsumeThreadMin(consumerConfig.getConsumeThreadMin());
            consumer.setConsumeThreadMax(consumerConfig.getConsumeThreadMax());
            consumer.setConsumeMessageBatchMaxSize(consumerConfig.getConsumeMessageBatchMaxSize());
            consumer.setMaxReconsumeTimes(consumerConfig.getMaxReconsumeTimes());
            consumer.setPullBatchSize(consumerConfig.getPullBatchSize());
            consumer.setPullInterval(consumerConfig.getPullInterval());

            log.info("Created Consumer for cluster: {}, group: {})", clusterName, group);

            return consumer;
        } catch (Exception ex) {
            log.error("Create consumer failed, cluster={}, group={}", clusterName, group, ex);
            throw new RuntimeException("Create consumer failed, cluster=" + clusterName, ex);
        }
    }

    private DtpExecutor getExecutor(String clusterName, String threadPoolName) {
        if (CharSequenceUtil.isBlank(threadPoolName)) {
            return null;
        }

        try {
            return DtpRegistry.getDtpExecutor(threadPoolName);
        } catch (Exception e) {
            log.error("Cluster {}: DtpExecutor '{}' not found", clusterName, threadPoolName, e);
        }
        return null;
    }

    private <K, V> V getOrThrow(
        Map<K, V> map,
        K key,
        RocketCodeEnum code) {

        return Optional.ofNullable(map.get(key))
            .orElseThrow(() -> new BizException(code));
    }
}
