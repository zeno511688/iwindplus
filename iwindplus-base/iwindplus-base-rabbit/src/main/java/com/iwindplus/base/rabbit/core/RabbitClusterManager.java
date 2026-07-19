/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.core;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.rabbit.domain.enums.RabbitCodeEnum;
import com.iwindplus.base.rabbit.domain.property.RabbitMultiProperty;
import com.iwindplus.base.rabbit.domain.property.RabbitMultiProperty.RabbitConsumerConfig;
import com.iwindplus.base.rabbit.domain.property.RabbitMultiProperty.RabbitMultiClusterConfig;
import com.iwindplus.base.rabbit.domain.property.RabbitMultiProperty.RabbitProducerConfig;
import com.iwindplus.base.rabbit.support.RabbitDynamicRegistry;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * Rabbit集群管理器.
 *
 * @author zengdegui
 * @since 2026/03/21 22:01
 */
@Slf4j
@RequiredArgsConstructor
public class RabbitClusterManager implements SmartLifecycle, DisposableBean {

    private final RabbitMultiProperty property;

    private final Map<String, AmqpAdmin> adminClientMap = new ConcurrentHashMap<>(16);
    private final Map<String, RabbitTemplate> templateMap = new ConcurrentHashMap<>(16);
    private final Map<String, SimpleRabbitListenerContainerFactory> factoryMap = new ConcurrentHashMap<>(16);

    private volatile boolean running = false;

    @Override
    public void start() {
        initClusters();
        running = true;
    }

    @Override
    public void stop() {
        running = false;

        templateMap.values().forEach(template -> {
            try {
                template.destroy();
            } catch (Exception e) {
                log.error("Close RabbitTemplate error", e);
            }
        });

        adminClientMap.clear();
        templateMap.clear();
        factoryMap.clear();
    }

    @Override
    public int getPhase() {
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
    }

    /**
     * 初始化所有集群.
     */
    public void initClusters() {
        if (Boolean.FALSE.equals(property.getEnabled())) {
            log.warn("Rabbit multi disabled");
            return;
        }

        Map<String, RabbitMultiClusterConfig> clusters = property.getClusters();
        if (MapUtil.isEmpty(clusters)) {
            throw new BizException(RabbitCodeEnum.RABBIT_CLUSTER_NOT_EXIST);
        }

        clusters.forEach((clusterName, clusterConfig) -> {
            CachingConnectionFactory connectionFactory = buildConnectionFactory(clusterName, clusterConfig);
            validateConnection(clusterName, connectionFactory);

            if (Boolean.TRUE.equals(property.getEnabledDynamicRegister())) {
                AmqpAdmin admin = buildAdminClient(clusterName, connectionFactory);
                RabbitDynamicRegistry.createBindingIfAbsent(clusterName, clusterConfig, admin);
            }

            buildRabbitTemplate(clusterName, connectionFactory, clusterConfig.getProducer());
            buildListenerContainerFactory(clusterName, connectionFactory, clusterConfig.getConsumer());
        });

        log.info("Rabbit clusters initialized: {}", clusters.size());
    }

    /**
     * 获取属性.
     *
     * @return RabbitMultiProperty
     */
    public RabbitMultiProperty getProperty() {
        return property;
    }

    /**
     * 获取AmqpAdmin.
     *
     * @return AmqpAdmin
     */
    public AmqpAdmin getAdmin(String cluster) {
        return getOrThrow(
            adminClientMap,
            resolveCluster(cluster),
            RabbitCodeEnum.RABBIT_CLUSTER_NOT_EXIST
        );
    }

    /**
     * 获取集群的Template.
     *
     * @return RabbitTemplate
     */
    public RabbitTemplate getTemplate(String cluster) {
        return getOrThrow(
            templateMap,
            resolveCluster(cluster),
            RabbitCodeEnum.RABBIT_CLUSTER_NOT_EXIST
        );
    }

    /**
     * 获取监听工厂.
     *
     * @param cluster 集群名称
     * @return SimpleRabbitListenerContainerFactory
     */
    public SimpleRabbitListenerContainerFactory getFactory(String cluster) {
        return getOrThrow(
            factoryMap,
            resolveCluster(cluster),
            RabbitCodeEnum.RABBIT_CONNECTION_NOT_EXIST
        );
    }

    /**
     * 获取默认集群.
     *
     * @return 默认集群
     */
    public String getDefaultCluster() {
        return property.getDefaultCluster();
    }

    /**
     * 获取所有集群名称.
     *
     * @return Map<String, RabbitMultiClusterConfig>
     */
    public Map<String, RabbitMultiClusterConfig> getAllClusters() {
        return property.getClusters();
    }

    /**
     * 获取并发数.
     *
     * @param cluster 集群名称
     * @return Integer
     */
    public Integer getConcurrency(String cluster) {
        return property.getConcurrency(cluster);
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
     * 获取批量监听器开关.
     *
     * @param cluster 集群名称
     * @return String
     */
    public boolean getEnabledBatchListener(String cluster) {
        return property.getEnabledBatchListener(cluster);
    }

    private void validateConnection(String clusterName, ConnectionFactory connectionFactory) {
        try {
            try (var connection = connectionFactory.createConnection();
                var channel = connection.createChannel(false)) {
                // 强制执行一次 AMQP 命令（真正验证 broker 可用）
                channel.basicQos(1);

                log.info("Rabbit connection OK: {}", clusterName);
            }
        } catch (Exception e) {
            log.error("Rabbit connection FAILED: {}", clusterName, e);
            throw new BizException(RabbitCodeEnum.RABBIT_CONNECTION_NOT_EXIST);
        }
    }

    private String resolveCluster(String cluster) {
        return CharSequenceUtil.isBlank(cluster)
            ? property.getDefaultCluster()
            : cluster;
    }

    private AmqpAdmin buildAdminClient(String clusterName, ConnectionFactory cf) {
        return adminClientMap.computeIfAbsent(clusterName, k -> new RabbitAdmin(cf));
    }

    private void buildRabbitTemplate(String clusterName,
        ConnectionFactory cf,
        RabbitProducerConfig producer) {

        if (producer == null || Boolean.FALSE.equals(producer.getEnabled())) {
            return;
        }

        templateMap.computeIfAbsent(clusterName, k -> {
            RabbitTemplate template = new RabbitTemplate(cf);
            template.setMandatory(producer.getMandatory());
            template.setMessageConverter(new Jackson2JsonMessageConverter());
            template.setReplyTimeout(producer.getReplyTimeout());

            if (Boolean.TRUE.equals(producer.getEnableRetry())) {
                RetryTemplate retryTemplate = new RetryTemplate();
                // 最多重试5次（第一次 + 重试4次）
                SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
                retryPolicy.setMaxAttempts(producer.getRetryAttempts());
                ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
                backOffPolicy.setInitialInterval(producer.getInitialInterval());
                backOffPolicy.setMultiplier(2.0);
                backOffPolicy.setMaxInterval(producer.getMaxInterval());
                retryTemplate.setRetryPolicy(retryPolicy);
                retryTemplate.setBackOffPolicy(backOffPolicy);
                template.setRetryTemplate(retryTemplate);

                // 最终失败回调
                template.setRecoveryCallback(context -> {
                    Throwable e = context.getLastThrowable();
                    log.error("RabbitMQ消息发送失败，已达到最大重试次数", e);

                    // 可以记录DB、发告警、写MQ补偿表等
                    return null;
                });
            }

            return template;
        });
    }

    private CachingConnectionFactory buildConnectionFactory(
        String clusterName,
        RabbitMultiClusterConfig config) {

        CachingConnectionFactory cf;

        if (CharSequenceUtil.isNotBlank(config.getAddresses())) {
            cf = new CachingConnectionFactory();
            cf.setAddresses(config.getAddresses());
        } else {
            cf = new CachingConnectionFactory(config.getHost(), config.getPort());
        }

        cf.setUsername(config.getUsername());
        cf.setPassword(config.getPassword());
        cf.setVirtualHost(config.getVirtualHost());
        cf.setRequestedHeartBeat(config.getRequestedHeartbeat());
        cf.setChannelCacheSize(config.getChannelCacheSize());
        // confirm / return
        final RabbitProducerConfig producerConfig = config.getProducer();
        if (producerConfig != null) {
            cf.setPublisherConfirmType(producerConfig.getPublisherConfirmType());
            cf.setPublisherReturns(producerConfig.getPublisherReturns());
        }

        log.info("CachingConnectionFactory built for cluster [{}]", clusterName);
        return cf;
    }

    private void buildListenerContainerFactory(
        String clusterName,
        ConnectionFactory cf,
        RabbitConsumerConfig consumer) {

        if (consumer == null || Boolean.FALSE.equals(consumer.getEnabled())) {
            return;
        }

        factoryMap.computeIfAbsent(clusterName, k -> {
            SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

            factory.setConnectionFactory(cf);

            factory.setConcurrentConsumers(Optional.ofNullable(consumer.getConcurrency()).orElse(1));
            factory.setMaxConcurrentConsumers(Optional.ofNullable(consumer.getMaxConcurrency()).orElse(5));
            factory.setPrefetchCount(Optional.ofNullable(consumer.getPrefetch()).orElse(50));

            factory.setAcknowledgeMode(
                Optional.ofNullable(consumer.getAcknowledgeMode())
                    .orElse(AcknowledgeMode.AUTO)
            );

            final DtpExecutor executor = getExecutor(clusterName, consumer.getThreadPoolName());
            if (executor != null) {
                factory.setTaskExecutor(executor);
            }

            buildAdviceChain(clusterName, consumer, factory);

            factory.setMessageConverter(new Jackson2JsonMessageConverter());

            log.info("SimpleRabbitListenerContainerFactory built for cluster [{}]", clusterName);
            return factory;
        });
    }

    private void buildAdviceChain(
        String clusterName,
        RabbitConsumerConfig consumer,
        SimpleRabbitListenerContainerFactory containerFactory) {
        if (Boolean.TRUE.equals(consumer.getEnableRetry())) {
            Advice retryAdvice = this.getRetryOperationsInterceptor(clusterName, consumer);
            containerFactory.setAdviceChain(retryAdvice);
        }
    }

    private DtpExecutor getExecutor(String clusterName, String poolName) {
        if (CharSequenceUtil.isBlank(poolName)) {
            return null;
        }

        try {
            return DtpRegistry.getDtpExecutor(poolName);
        } catch (Exception e) {
            log.error("Cluster {}: DtpExecutor '{}' not found", clusterName, poolName, e);
        }
        return null;
    }

    private RetryOperationsInterceptor getRetryOperationsInterceptor(
        String clusterName,
        RabbitConsumerConfig consumer) {

        return RetryInterceptorBuilder.stateless()
            .retryOperations(getRetryTemplate(
                consumer.getRetryAttempts(),
                consumer.getRetryInterval()
            ))
            .recoverer((msg, cause) ->
                log.error("[{}] Consumer failed after retry", clusterName, cause)
            )
            .build();
    }

    private RetryTemplate getRetryTemplate(Integer attempts, Long interval) {
        RetryTemplate template = new RetryTemplate();

        SimpleRetryPolicy policy = new SimpleRetryPolicy();
        policy.setMaxAttempts(Optional.ofNullable(attempts).orElse(3));

        FixedBackOffPolicy backOff = new FixedBackOffPolicy();
        backOff.setBackOffPeriod(Optional.ofNullable(interval).orElse(1000L));

        template.setRetryPolicy(policy);
        template.setBackOffPolicy(backOff);

        return template;
    }

    private <K, V> V getOrThrow(
        Map<K, V> map,
        K key,
        RabbitCodeEnum code) {

        return Optional.ofNullable(map.get(key))
            .orElseThrow(() -> new BizException(code));
    }
}