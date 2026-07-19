/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.domain.property;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.kafka.domain.constant.KafkaConstant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.kafka.listener.ContainerProperties.AckMode;

/**
 * 多集群Kafka配置（全参数可配置版）. 所有Kafka原生参数都可通过 properties Map 自定义覆盖
 *
 * @author zengdegui
 * @since 2020/4/24
 */
@Slf4j
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "kafka.multi")
public class KafkaMultiProperty {

    /**
     * 是否开启.
     */
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 默认集群（必填）.
     */
    private String defaultCluster;

    /**
     * 是否开启动态注册（创建Topic）.
     */
    @Builder.Default
    private Boolean enabledDynamicRegister = false;

    /**
     * 集群配置.
     */
    @NestedConfigurationProperty
    private Map<String, KafkaMultiClusterConfig> clusters;

    /**
     * 多集群Kafka集群配置对象.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KafkaMultiClusterConfig {

        /**
         * 集群地址.
         */
        private String bootstrapServers;

        /**
         * 生产者配置.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private KafkaProducerConfig producer = new KafkaProducerConfig();

        /**
         * 消费者配置.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private KafkaConsumerConfig consumer = new KafkaConsumerConfig();
    }

    /**
     * 生产者配置.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KafkaProducerConfig {

        /**
         * 是否开启.
         */
        @Builder.Default
        private Boolean enabled = true;

        /**
         * 是否启用每次请求观察（自带的）.
         */
        @Builder.Default
        private Boolean enabledObservation = Boolean.TRUE;

        /**
         * Bootstrap服务器（覆盖集群配置，可选）.
         */
        private String bootstrapServers;

        /**
         * 客户端ID（可选）.
         */
        private String clientId;

        /**
         * 默认Topic（可选，默认发送）.
         */
        private String defaultTopic;

        /**
         * Key序列化器类名.
         */
        @Builder.Default
        private String keySerializer = "org.apache.kafka.common.serialization.StringSerializer";

        /**
         * Value序列化器类名.
         */
        @Builder.Default
        private String valueSerializer = "org.apache.kafka.common.serialization.StringSerializer";

        /**
         * acks: 0, 1, all.
         */
        @Builder.Default
        private String acks = "all";

        /**
         * 重试次数.
         */
        @Builder.Default
        private Integer retries = Integer.MAX_VALUE;

        /**
         * 幂等性.
         */
        @Builder.Default
        private Boolean enableIdempotence = true;

        /**
         * 是否开启事务.
         */
        @Builder.Default
        private Boolean enableTransaction = false;

        /**
         * 事务ID前缀.
         */
        private String transactionIdPrefix;

        /**
         * 单个 partition 批量缓冲的最大大小（字节）.
         */
        @Builder.Default
        private Integer batchSize = 16384;

        /**
         * 发送延迟（毫秒）.
         */
        @Builder.Default
        private Integer lingerMs = 5;

        /**
         * 缓冲区大小（字节）.
         */
        @Builder.Default
        private Long bufferMemory = 67108864L;

        /**
         * 压缩类型: none, gzip, snappy, lz4, zstd.
         */
        @Builder.Default
        private String compressionType = "lz4";

        /**
         * 最大请求大小（字节）.
         */
        @Builder.Default
        private Integer maxRequestSize = 1048576;

        /**
         * 最大并发请求数.
         */
        @Builder.Default
        private Integer maxInFlightRequestsPerConnection = 5;

        /**
         * 最大阻塞时间（毫秒）.
         */
        @Builder.Default
        private Integer maxBlockMs = 60000;

        /**
         * 请求超时（毫秒）.
         */
        @Builder.Default
        private Integer requestTimeoutMs = 30000;

        /**
         * 元数据最大年龄（毫秒）.
         */
        @Builder.Default
        private Integer metadataMaxAgeMs = 300000;

        /**
         * 重试间隔（毫秒）.
         */
        @Builder.Default
        private Long retryBackoffMs = 100L;

        /**
         * 重连退避时间（毫秒）.
         */
        @Builder.Default
        private Long reconnectBackoffMs = 50L;

        /**
         * 重连退避最大时间（毫秒）.
         */
        @Builder.Default
        private Long reconnectBackoffMaxMs = 1000L;

        /**
         * Socket发送缓冲区（字节）.
         */
        @Builder.Default
        private Integer sendBufferBytes = 131072;

        /**
         * Socket接收缓冲区（字节）.
         */
        @Builder.Default
        private Integer receiveBufferBytes = 65536;

        /**
         * 连接最大空闲时间（毫秒）.
         */
        @Builder.Default
        private Integer connectionsMaxIdleMs = 540000;

        /**
         * 自定义扩展配置（覆盖所有以上配置）. 可配置任何Kafka原生参数
         */
        @Builder.Default
        @NestedConfigurationProperty
        private Map<String, Object> properties = new HashMap<>(16);
    }

    /**
     * 消费者配置.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KafkaConsumerConfig {

        /**
         * 是否开启.
         */
        @Builder.Default
        private Boolean enabled = true;

        /**
         * 是否启用每次请求观察（自带的）.
         */
        @Builder.Default
        private Boolean enabledObservation = Boolean.TRUE;

        /**
         * Bootstrap服务器（覆盖集群配置，可选）.
         */
        private String bootstrapServers;

        /**
         * 客户端ID（可选）.
         */
        private String clientId;

        /**
         * Key反序列化器类名.
         */
        @Builder.Default
        private String keyDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";

        /**
         * Value反序列化器类名.
         */
        @Builder.Default
        private String valueDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";

        /**
         * 监听线程池bean名称（对于DtpExecutor）.
         */
        private String threadPoolName;

        /**
         * 自动偏移重置: earliest, latest, none.
         */
        @Builder.Default
        private String autoOffsetReset = "earliest";

        /**
         * 是否自动提交.
         */
        @Builder.Default
        private Boolean enabledAutoCommit = false;

        /**
         * 自动提交间隔（毫秒）.
         */
        @Builder.Default
        private Integer autoCommitIntervalMs = 1000;

        /**
         * 单次poll最大记录数.
         */
        @Builder.Default
        private Integer maxPollRecords = 500;

        /**
         * 两次poll最大间隔（毫秒，防止处理慢导致rebalance）.
         */
        @Builder.Default
        private Integer maxPollIntervalMs = 300000;

        /**
         * 单次拉取最小字节数.
         */
        @Builder.Default
        private Integer fetchMinBytes = 1;

        /**
         * 单次拉取最大字节数.
         */
        @Builder.Default
        private Integer fetchMaxBytes = 524288;

        /**
         * 每分区最大拉取字节数.
         */
        @Builder.Default
        private Integer maxPartitionFetchBytes = 1048576;

        /**
         * 拉取等待最大时间（毫秒）.
         */
        @Builder.Default
        private Integer fetchMaxWaitMs = 500;

        /**
         * 会话超时（毫秒）.
         */
        @Builder.Default
        private Integer sessionTimeoutMs = 10000;

        /**
         * 心跳间隔（毫秒）.
         */
        @Builder.Default
        private Integer heartbeatIntervalMs = 3000;

        /**
         * 并发消费者数.
         */
        @Builder.Default
        private Integer concurrency = 3;

        /**
         * 是否批量监听.
         */
        @Builder.Default
        private Boolean enabledBatchListener = true;

        /**
         * Ack模式.
         */
        @Builder.Default
        private AckMode ackMode = AckMode.MANUAL;

        /**
         * 批量确认数量阈值.
         */
        @Builder.Default
        private Integer ackCount = 100;

        /**
         * 批量确认时间阈值（毫秒）.
         */
        @Builder.Default
        private Long ackTime = 5000L;

        /**
         * 是否异步提交.
         */
        @Builder.Default
        private Boolean enableAsyncAcks = false;

        /**
         * Poll超时（毫秒）.
         */
        @Builder.Default
        private Long pollTimeoutMs = 1000L;

        /**
         * Socket接收缓冲区（字节）.
         */
        @Builder.Default
        private Integer receiveBufferBytes = 65536;

        /**
         * Socket发送缓冲区（字节）.
         */
        @Builder.Default
        private Integer sendBufferBytes = 131072;

        /**
         * 默认API超时时间（毫秒）.
         */
        @Builder.Default
        private Integer defaultApiTimeoutMs = 60000;

        /**
         * 连接最大空闲时间（毫秒）.
         */
        @Builder.Default
        private Integer connectionsMaxIdleMs = 540000;

        /**
         * 分区分配策略类名.
         */
        @Builder.Default
        private String partitionAssignmentStrategy = "org.apache.kafka.clients.consumer.CooperativeStickyAssignor";

        /**
         * 元数据最大年龄（毫秒）.
         */
        @Builder.Default
        private Integer metadataMaxAgeMs = 300000;

        /**
         * 自定义扩展配置（覆盖所有以上配置）. 可配置任何Kafka原生参数
         */
        @Builder.Default
        @NestedConfigurationProperty
        private Map<String, Object> properties = new HashMap<>(16);

        /**
         * 绑定关系配置.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private List<KafkaBindingConfig> bindings = new ArrayList<>(10);
    }

    /**
     * 绑定关系配置.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KafkaBindingConfig {

        /**
         * 是否自动创建.
         */
        @Builder.Default
        private Boolean autoCreate = true;

        /**
         * Topic名称.
         */
        private String topic;

        /**
         * 消费组（覆盖集群配置）.
         */
        private String group;

        /**
         * 副本数量.
         */
        @Builder.Default
        private Short replicationFactor = -1;

        /**
         * 分区数量.
         */
        @Builder.Default
        private Integer partitions = -1;

        /**
         * 其他自定义参数.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private Map<String, String> arguments = new HashMap<>(16);

        /**
         * 是否启用重试.
         */
        @Builder.Default
        private Boolean enabledRetry = true;

        /**
         * 是否启用死信队列.
         */
        @Builder.Default
        private Boolean enabledDlq = true;

        /**
         * 最大重试次数.
         */
        @Builder.Default
        private Integer retryMaxCount = 10;
    }

    /**
     * 构建生产者配置（全参数可配置）. 优先级: 自定义 properties > 显式配置 > Kafka默认值
     */
    public Map<String, Object> buildProducerProps(String cluster) {
        final KafkaMultiClusterConfig clusterConfig = clusters.get(cluster);
        if (clusterConfig == null) {
            throw new IllegalArgumentException("Cluster not found: " + cluster);
        }

        final KafkaProducerConfig p = clusterConfig.getProducer();
        Map<String, Object> config = new HashMap<>(16);

        // 基础配置（可被覆盖）
        putIfNotNull(config, ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            p.getBootstrapServers() != null ? p.getBootstrapServers() : clusterConfig.getBootstrapServers());

        putIfNotNull(config, ProducerConfig.CLIENT_ID_CONFIG, getProducerClientId(cluster));

        // 序列化配置（可被覆盖）
        putIfNotNull(config, ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, p.getKeySerializer());
        putIfNotNull(config, ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, p.getValueSerializer());

        // 可靠性配置（可被覆盖）
        putIfNotNull(config, ProducerConfig.ACKS_CONFIG, p.getAcks());
        putIfNotNull(config, ProducerConfig.RETRIES_CONFIG, p.getRetries());
        putIfNotNull(config, ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, p.getEnableIdempotence());

        // 批量性能配置（可被覆盖）
        putIfNotNull(config, ProducerConfig.BATCH_SIZE_CONFIG, p.getBatchSize());
        putIfNotNull(config, ProducerConfig.LINGER_MS_CONFIG, p.getLingerMs());
        putIfNotNull(config, ProducerConfig.BUFFER_MEMORY_CONFIG, p.getBufferMemory());
        putIfNotNull(config, ProducerConfig.COMPRESSION_TYPE_CONFIG, p.getCompressionType());

        // 请求配置（可被覆盖）
        putIfNotNull(config, ProducerConfig.MAX_REQUEST_SIZE_CONFIG, p.getMaxRequestSize());
        putIfNotNull(config, ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, p.getMaxInFlightRequestsPerConnection());

        // 超时配置（可被覆盖）
        putIfNotNull(config, ProducerConfig.MAX_BLOCK_MS_CONFIG, p.getMaxBlockMs());
        putIfNotNull(config, ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, p.getRequestTimeoutMs());
        putIfNotNull(config, ProducerConfig.METADATA_MAX_AGE_CONFIG, p.getMetadataMaxAgeMs());
        putIfNotNull(config, ProducerConfig.RETRY_BACKOFF_MS_CONFIG, p.getRetryBackoffMs());
        putIfNotNull(config, ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, p.getReconnectBackoffMs());
        putIfNotNull(config, ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, p.getReconnectBackoffMaxMs());

        // 连接配置（可被覆盖）
        putIfNotNull(config, ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, p.getConnectionsMaxIdleMs());
        putIfNotNull(config, ProducerConfig.SEND_BUFFER_CONFIG, p.getSendBufferBytes());
        putIfNotNull(config, ProducerConfig.RECEIVE_BUFFER_CONFIG, p.getReceiveBufferBytes());

        // 事务配置（可被覆盖）
        if (Boolean.TRUE.equals(p.getEnableTransaction())) {
            putIfNotNull(config, ProducerConfig.TRANSACTIONAL_ID_CONFIG, p.getTransactionIdPrefix());
        }

        // 自定义扩展配置（最高优先级，覆盖所有以上配置）
        if (p.getProperties() != null && !p.getProperties().isEmpty()) {
            log.debug("Applying custom producer properties for cluster {}: {}", cluster, p.getProperties());
            config.putAll(p.getProperties());
        }

        return config;
    }

    /**
     * 构建消费者配置（全参数可配置）. 优先级: 自定义 properties > 显式配置 > Kafka默认值
     */
    public Map<String, Object> buildConsumerProps(String cluster) {
        final KafkaMultiClusterConfig clusterConfig = clusters.get(cluster);
        if (clusterConfig == null) {
            throw new IllegalArgumentException("Cluster not found: " + cluster);
        }

        final KafkaConsumerConfig c = clusterConfig.getConsumer();
        Map<String, Object> config = new HashMap<>(16);

        // 基础配置（可被覆盖）
        putIfNotNull(config, ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
            c.getBootstrapServers() != null ? c.getBootstrapServers() : clusterConfig.getBootstrapServers());

        putIfNotNull(config, ConsumerConfig.CLIENT_ID_CONFIG, getConsumerClientId(cluster));

        // 序列化配置（可被覆盖）
        putIfNotNull(config, ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, c.getKeyDeserializer());
        putIfNotNull(config, ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, c.getValueDeserializer());

        // 偏移量管理（可被覆盖）
        putIfNotNull(config, ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, c.getAutoOffsetReset());
        putIfNotNull(config, ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, c.getEnabledAutoCommit());
        putIfNotNull(config, ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, c.getAutoCommitIntervalMs());

        // 拉取性能配置（可被覆盖）
        putIfNotNull(config, ConsumerConfig.MAX_POLL_RECORDS_CONFIG, c.getMaxPollRecords());
        putIfNotNull(config, ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, c.getMaxPollIntervalMs());
        putIfNotNull(config, ConsumerConfig.FETCH_MIN_BYTES_CONFIG, c.getFetchMinBytes());
        putIfNotNull(config, ConsumerConfig.FETCH_MAX_BYTES_CONFIG, c.getFetchMaxBytes());
        putIfNotNull(config, ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, c.getMaxPartitionFetchBytes());
        putIfNotNull(config, ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, c.getFetchMaxWaitMs());

        // 心跳与会话（可被覆盖）
        putIfNotNull(config, ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, c.getHeartbeatIntervalMs());
        putIfNotNull(config, ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, c.getSessionTimeoutMs());

        // 连接配置（可被覆盖）
        putIfNotNull(config, ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, c.getConnectionsMaxIdleMs());
        putIfNotNull(config, ConsumerConfig.RECEIVE_BUFFER_CONFIG, c.getReceiveBufferBytes());
        putIfNotNull(config, ConsumerConfig.SEND_BUFFER_CONFIG, c.getSendBufferBytes());
        putIfNotNull(config, ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, c.getDefaultApiTimeoutMs());

        // 分区分配（可被覆盖）
        putIfNotNull(config, ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, c.getPartitionAssignmentStrategy());
        putIfNotNull(config, ConsumerConfig.METADATA_MAX_AGE_CONFIG, c.getMetadataMaxAgeMs());

        // 自定义扩展配置（最高优先级，覆盖所有以上配置）
        if (c.getProperties() != null && !c.getProperties().isEmpty()) {
            log.debug("Applying custom consumer properties for cluster {}: {}", cluster, c.getProperties());
            config.putAll(c.getProperties());
        }

        return config;
    }

    /**
     * 获取集群配置
     *
     * @param cluster 集群
     * @return
     */
    public KafkaMultiClusterConfig getClusterConfig(String cluster) {
        return clusters.get(cluster);
    }

    /**
     * 获取生产者配置
     *
     * @param cluster 集群
     * @return
     */
    public KafkaProducerConfig getProducerConfig(String cluster) {
        KafkaMultiClusterConfig config = clusters.get(cluster);
        return config != null && config.getProducer() != null
            ? config.getProducer() : new KafkaProducerConfig();
    }

    /**
     * 获取消费者配置
     *
     * @param cluster 集群
     * @return
     */
    public KafkaConsumerConfig getConsumerConfig(String cluster) {
        KafkaMultiClusterConfig config = clusters.get(cluster);
        return config != null && config.getConsumer() != null
            ? config.getConsumer() : new KafkaConsumerConfig();
    }

    /**
     * 获取绑定关系配置
     *
     * @param cluster 集群
     * @return
     */
    public List<KafkaBindingConfig> listBindingConfig(String cluster) {
        final KafkaConsumerConfig config = getConsumerConfig(cluster);
        return config != null && config.getBindings() != null
            ? config.getBindings() : new ArrayList<>(10);
    }

    /**
     * 添加配置项
     *
     * @param config 配置项
     * @param key    键
     * @param value  值
     */
    private void putIfNotNull(Map<String, Object> config, String key, Object value) {
        if (value != null) {
            config.put(key, value);
        }
    }

    /**
     * 获取ack模式
     *
     * @param cluster 集群
     * @return
     */
    public AckMode getAckMode(String cluster) {
        final KafkaConsumerConfig config = getConsumerConfig(cluster);
        return config != null && config.getAckMode() != null
            ? config.getAckMode() : AckMode.MANUAL_IMMEDIATE;
    }

    /**
     * 获取并发数
     *
     * @param cluster 集群
     * @return
     */
    public Integer getConcurrency(String cluster) {
        final KafkaConsumerConfig config = getConsumerConfig(cluster);
        return config != null && config.getConcurrency() != null
            ? config.getConcurrency() : 1;
    }

    /**
     * 获取自动提交
     *
     * @param cluster 集群
     * @return
     */
    public Boolean getAutoCommit(String cluster) {
        final KafkaConsumerConfig config = getConsumerConfig(cluster);
        return config != null && config.getEnabledAutoCommit() != null
            && Boolean.TRUE.equals(config.getEnabledAutoCommit());
    }

    /**
     * 获取生产者clientId
     *
     * @param cluster 集群
     * @return
     */
    public String getProducerClientId(String cluster) {
        final KafkaProducerConfig config = getProducerConfig(cluster);

        return CharSequenceUtil.isBlank(config.getClientId())
            ? cluster + KafkaConstant.PRODUCER_SUFFIX : config.getClientId();
    }

    /**
     * 获取消费者clientId
     *
     * @param cluster 集群
     * @return
     */
    public String getConsumerClientId(String cluster) {
        final KafkaConsumerConfig config = getConsumerConfig(cluster);

        return CharSequenceUtil.isBlank(config.getClientId())
            ? cluster + KafkaConstant.CONSUMER_SUFFIX : config.getClientId();
    }

    /**
     * 是否启用批量监听
     *
     * @param cluster 集群
     * @return
     */
    public Boolean getEnabledBatchListener(String cluster) {
        final KafkaConsumerConfig config = getConsumerConfig(cluster);
        return config != null && Boolean.TRUE.equals(config.getEnabledBatchListener());
    }

    /**
     * 是否启用监控观察（自带的）
     *
     * @param cluster 集群
     * @return
     */
    public Boolean getProducerEnabledObservation(String cluster) {
        final KafkaProducerConfig config = getProducerConfig(cluster);
        return config != null && config.getEnabledObservation();
    }

    /**
     * 是否启用监控观察（自带的）
     *
     * @param cluster 集群
     * @return
     */
    public Boolean getConsumerEnabledObservation(String cluster) {
        final KafkaConsumerConfig config = getConsumerConfig(cluster);
        return config != null && config.getEnabledObservation();
    }

    /**
     * 获取Topic集合
     *
     * @param cluster 集群
     * @return
     */
    public List<String> listTopic(String cluster) {
        if (CharSequenceUtil.isBlank(cluster)) {
            return new ArrayList<>();
        }

        final KafkaConsumerConfig config = getConsumerConfig(cluster);
        if (config == null) {
            return new ArrayList<>();
        }

        final List<KafkaBindingConfig> bindings = config.getBindings();
        if (CollUtil.isEmpty(bindings)) {
            return new ArrayList<>();
        }

        return bindings.stream()
            .filter(binding -> binding != null && CharSequenceUtil.isNotBlank(binding.getTopic()))
            .map(KafkaBindingConfig::getTopic)
            .collect(Collectors.toList());
    }
}