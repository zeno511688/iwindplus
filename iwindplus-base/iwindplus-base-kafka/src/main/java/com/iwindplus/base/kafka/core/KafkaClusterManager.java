/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.kafka.domain.enums.KafkaCodeEnum;
import com.iwindplus.base.kafka.domain.enums.KafkaConsumerLocalRetryTypeEnum;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty.KafkaConsumerConfig;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty.KafkaConsumerLocalRetryConfig;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty.KafkaMultiClusterConfig;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty.KafkaProducerConfig;
import com.iwindplus.base.kafka.support.KafkaDynamicRegistry;
import com.iwindplus.base.kafka.support.KafkaErrorHandler;
import com.iwindplus.base.kafka.support.observation.CustomKafkaListenerObservationConvention;
import com.iwindplus.base.kafka.support.observation.CustomKafkaTemplateObservationConvention;
import io.micrometer.observation.ObservationRegistry;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Kafka 集群管理器（工业级增强版）.
 *
 * @author zengdegui
 * @since 2026/03/21 22:01
 */
@Slf4j
@RequiredArgsConstructor
public class KafkaClusterManager implements SmartLifecycle, DisposableBean {

    private final KafkaMultiProperty property;
    private final ObservationRegistry observationRegistry;
    private final ApplicationContext applicationContext;

    private final Map<String, AdminClient> adminClientMap = new ConcurrentHashMap<>(16);
    private final Map<String, KafkaTemplate<String, Object>> templateMap = new ConcurrentHashMap<>(16);
    private final Map<String, ConcurrentKafkaListenerContainerFactory<String, Object>> factoryMap = new ConcurrentHashMap<>(16);

    private volatile boolean running = false;

    @Override
    public void start() {

        initClusters();

        running = true;

        log.info("KafkaClusterManager started");
    }

    @Override
    public void stop() {

        running = false;

        adminClientMap.values().forEach(client -> {
            try {
                client.close(Duration.ofSeconds(5));
            } catch (Exception e) {
                log.error("Close AdminClient error", e);
            }
        });

        templateMap.values().forEach(template -> {
            try {
                template.destroy();
            } catch (Exception e) {
                log.error("Destroy KafkaTemplate error", e);
            }
        });

        log.info("KafkaClusterManager stopped");
    }

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void destroy() {
        stop();
    }

    /**
     * 初始化所有集群.
     */
    public void initClusters() {
        if (Boolean.FALSE.equals(property.getEnabled())) {
            log.warn("Kafka multi disabled");

            return;
        }

        final Map<String, KafkaMultiClusterConfig> clusters =
            property.getClusters();

        if (MapUtil.isEmpty(clusters)) {
            throw new BizException(KafkaCodeEnum.KAFKA_CLUSTER_NOT_EXIST);
        }

        clusters.forEach((clusterName, clusterConfig) -> {
            final AdminClient adminClient =
                buildAdminClient(
                    clusterName,
                    clusterConfig.getBootstrapServers()
                );

            if (Boolean.TRUE.equals(
                property.getEnabledDynamicRegister())) {

                KafkaDynamicRegistry.createTopicsIfAbsent(
                    clusterName,
                    clusterConfig,
                    adminClient,
                    10
                );
            }

            buildProducer(property, clusterName, clusterConfig.getProducer());

            buildConsumer(property, clusterName, clusterConfig.getConsumer());
        });

        log.info("Kafka clusters initialized: {}", clusters.size());
    }

    /**
     * 获取 AdminClient.
     */
    public AdminClient getAdmin(String cluster) {
        return getOrThrow(
            adminClientMap,
            resolveCluster(cluster),
            KafkaCodeEnum.KAFKA_CLUSTER_NOT_EXIST
        );
    }

    /**
     * 获取默认 AdminClient.
     */
    public AdminClient getDefaultAdmin() {
        return getAdmin(null);
    }

    /**
     * 获取集群ID.
     *
     * @param cluster 集群名称
     * @return String
     */
    public String getClusterId(String cluster) {
        AdminClient admin = getAdmin(cluster);
        try {
            return admin
                .describeCluster()
                .clusterId()
                .get();
        } catch (Exception e) {
            log.warn(
                "Get kafka clusterId failed, cluster={}",
                cluster,
                e
            );

            return null;
        }
    }

    /**
     * 获取消费者clientId
     *
     * @param cluster 集群
     * @return
     */
    public String getConsumerClientId(String cluster) {
        return property.getConsumerClientId(cluster);
    }

    /**
     * 获取同步 KafkaTemplate.
     */
    public KafkaTemplate<String, Object> getTemplate(String cluster) {
        return getOrThrow(
            templateMap,
            resolveCluster(cluster),
            KafkaCodeEnum.KAFKA_CLUSTER_NOT_EXIST
        );
    }

    /**
     * 获取默认 KafkaTemplate.
     */
    public KafkaTemplate<String, Object> getDefaultTemplate() {
        return getTemplate(null);
    }

    /**
     * 获取 Listener Factory.
     */
    public ConcurrentKafkaListenerContainerFactory<String, Object> getFactory(String cluster) {
        return getOrThrow(
            factoryMap,
            resolveCluster(cluster),
            KafkaCodeEnum.KAFKA_LISTENER_NOT_EXIST
        );
    }

    /**
     * 获取默认 Listener Factory.
     */
    public ConcurrentKafkaListenerContainerFactory<String, Object> getDefaultFactory() {
        return getFactory(getDefaultCluster());
    }

    /**
     * 获取默认集群.
     */
    public String getDefaultCluster() {
        return property.getDefaultCluster();
    }

    /**
     * 获取所有集群.
     */
    public Map<String, KafkaMultiClusterConfig> getAllClusters() {
        return property.getClusters();
    }

    /**
     * 获取并发数.
     */
    public Integer getConcurrency(String cluster) {
        return property.getConcurrency(cluster);
    }

    /**
     * 获取消费者配置.
     */
    public KafkaConsumerConfig getConsumerConfig(String cluster) {
        return property.getConsumerConfig(cluster);
    }

    /**
     * 获取配置.
     */
    public KafkaMultiProperty getProperty() {
        return property;
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
     * resolve cluster.
     */
    private String resolveCluster(String cluster) {
        return CharSequenceUtil.isBlank(cluster)
            ? property.getDefaultCluster()
            : cluster;
    }

    /**
     * build admin.
     */
    private AdminClient buildAdminClient(
        String clusterName,
        String bootstrapServers) {
        return adminClientMap.computeIfAbsent(
            clusterName,
            key -> {
                Map<String, Object> adminProps = new HashMap<>(16);

                adminProps.put(
                    AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                    bootstrapServers
                );

                AdminClient admin = AdminClient.create(adminProps);

                log.info(
                    "AdminClient created cluster={}",
                    clusterName
                );

                return admin;
            }
        );
    }

    /**
     * build producer.
     */
    private void buildProducer(
        KafkaMultiProperty property,
        String clusterName,
        KafkaProducerConfig producer) {

        if (Objects.isNull(producer)
            || Boolean.FALSE.equals(
            producer.getEnabled())) {

            return;
        }

        templateMap.computeIfAbsent(
            clusterName,
            key -> {
                Map<String, Object> props = property.buildProducerProps(clusterName);

                DefaultKafkaProducerFactory<String, Object> factory =
                    new DefaultKafkaProducerFactory<>(props);

                KafkaTemplate<String, Object> template = new KafkaTemplate<>(factory);
                template.setApplicationContext(applicationContext);
                if (CharSequenceUtil.isNotBlank(producer.getDefaultTopic())) {
                    template.setDefaultTopic(producer.getDefaultTopic());
                }
                template.setObservationEnabled(producer.getEnabledObservation());
                template.setObservationConvention(new CustomKafkaTemplateObservationConvention());
                template.afterSingletonsInstantiated();

                log.info("KafkaTemplate created cluster={}, acks={}, compression={}",
                    clusterName,
                    producer.getAcks(),
                    producer.getCompressionType()
                );

                return template;
            }
        );
    }

    /**
     * build consumer.
     */
    private void buildConsumer(
        KafkaMultiProperty property,
        String clusterName,
        KafkaConsumerConfig consumer) {

        if (Objects.isNull(consumer)
            || Boolean.FALSE.equals(
            consumer.getEnabled())) {

            return;
        }

        factoryMap.computeIfAbsent(
            clusterName,
            key -> {
                Map<String, Object> props = property.buildConsumerProps(clusterName);

                DefaultKafkaConsumerFactory<String, Object>
                    consumerFactory =
                    new DefaultKafkaConsumerFactory<>(props);

                ConcurrentKafkaListenerContainerFactory<String, Object>
                    factory = new ConcurrentKafkaListenerContainerFactory<>();

                factory.setConsumerFactory(consumerFactory);
                factory.setConcurrency(consumer.getConcurrency());
                factory.setBatchListener(consumer.getEnabledBatchListener());
                // 消费重试
                buildErrorHandler(clusterName, consumer, factory);

                ContainerProperties containerProps = factory.getContainerProperties();
                buildContainerProperties(clusterName, consumer, containerProps);

                log.info("Kafka Consumer created cluster={}, concurrency={}, batch={}, ackMode={}",
                    clusterName,
                    consumer.getConcurrency(),
                    consumer.getEnabledBatchListener(),
                    consumer.getAckMode()
                );

                return factory;
            }
        );
    }

    private void buildErrorHandler(
        String clusterName,
        KafkaConsumerConfig consumer,
        ConcurrentKafkaListenerContainerFactory<String, Object> factory) {

        final KafkaConsumerLocalRetryConfig cfg = consumer.getLocalRetry();
        if (Boolean.FALSE.equals(cfg.getEnabled())) {
            return;
        }

        if (Boolean.TRUE.equals(consumer.getEnabledDlq())) {
            ConsumerRecordRecoverer recoverer = new KafkaErrorHandler(
                clusterName,
                consumer,
                applicationContext.getBean(KafkaTemplateRouter.class)
            );
            DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, this.buildBackOff(cfg));
            // 手动提交很重要这个参数，不然会导致发送dlq成功后offset没有提交，导致重复消费
            errorHandler.setCommitRecovered(true);
            factory.setCommonErrorHandler(errorHandler);
        } else {
            DefaultErrorHandler errorHandler = new DefaultErrorHandler(this.buildBackOff(cfg));
            errorHandler.setCommitRecovered(false);
            factory.setCommonErrorHandler(errorHandler);
        }
    }

    private BackOff buildBackOff(KafkaConsumerLocalRetryConfig cfg) {
        if (KafkaConsumerLocalRetryTypeEnum.FIXED.equals(cfg.getType())) {
            return new FixedBackOff(
                cfg.getIntervalMs(),
                cfg.getAttempts()
            );
        }

        if (KafkaConsumerLocalRetryTypeEnum.EXPONENTIAL.equals(cfg.getType())) {
            ExponentialBackOff backOff = new ExponentialBackOff();

            backOff.setInitialInterval(cfg.getIntervalMs());
            backOff.setMultiplier(cfg.getMultiplier());
            backOff.setMaxInterval(cfg.getMaxIntervalMs());

            return backOff;
        }

        throw new IllegalArgumentException("Unsupported backOff type: " + cfg.getType());
    }

    /**
     * build container props.
     */
    private void buildContainerProperties(
        String clusterName,
        KafkaConsumerConfig consumer,
        ContainerProperties containerProps) {

        containerProps.setClientId(this.getConsumerClientId(clusterName));
        containerProps.setPollTimeout(consumer.getPollTimeoutMs());
        // 自定义注解不起作用
        containerProps.setObservationEnabled(consumer.getEnabledObservation());
        containerProps.setObservationConvention(new CustomKafkaListenerObservationConvention());

        if (Boolean.FALSE.equals(
            consumer.getEnabledAutoCommit())) {
            AckMode ackMode = consumer.getAckMode();
            containerProps.setAckMode(ackMode);
            if (ackMode == AckMode.BATCH) {
                containerProps.setAckCount(consumer.getAckCount());
                containerProps.setAckTime(consumer.getAckTime());

            } else if (ackMode == AckMode.TIME) {
                containerProps.setAckTime(consumer.getAckTime());
            }

            containerProps.setAsyncAcks(
                Boolean.TRUE.equals(consumer.getEnableAsyncAcks())
            );

            log.info("Cluster {} ackMode={}, asyncAcks={}, ackCount={}, ackTime={}",
                clusterName,
                ackMode,
                consumer.getEnableAsyncAcks(),
                consumer.getAckCount(),
                consumer.getAckTime()
            );
        } else {
            log.warn("Cluster {} auto commit enabled",
                clusterName
            );
        }
        buildConsumerReBalanceListener(clusterName, containerProps);

        buildThreadPool(
            clusterName,
            consumer,
            containerProps
        );
    }

    /**
     * build thread pool.
     */
    private void buildThreadPool(
        String clusterName,
        KafkaConsumerConfig consumer,
        ContainerProperties containerProps) {

        final DtpExecutor executor = getExecutor(clusterName, consumer.getThreadPoolName());
        if (executor != null) {
            containerProps.setListenerTaskExecutor(
                new ConcurrentTaskExecutor(executor)
            );
        }
    }

    private void buildConsumerReBalanceListener(String clusterName, ContainerProperties containerProps) {
        containerProps.setConsumerRebalanceListener(
            new ConsumerRebalanceListener() {

                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                    if (CollUtil.isNotEmpty(partitions)) {
                        log.warn("Cluster {} revoked={}",
                            clusterName,
                            partitions
                        );
                    }
                }

                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                    if (CollUtil.isNotEmpty(partitions)) {
                        log.info("Cluster {} assigned={}",
                            clusterName,
                            partitions
                        );
                    }
                }
            }
        );
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
        KafkaCodeEnum code) {

        return Optional.ofNullable(map.get(key))
            .orElseThrow(() -> new BizException(code));
    }
}