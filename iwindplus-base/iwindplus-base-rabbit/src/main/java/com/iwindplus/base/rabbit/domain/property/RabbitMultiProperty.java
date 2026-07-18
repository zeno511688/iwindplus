/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.domain.property;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.rabbit.domain.constant.RabbitConstant;
import com.iwindplus.base.rabbit.domain.enums.RabbitExchangeTypeEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory.ConfirmType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 多集群rabbit配置.
 *
 * @author zengdegui
 * @since 2020/4/24
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "rabbit.multi")
public class RabbitMultiProperty {

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
     * 是否开启动态注册队列，交换机，绑定关系等.
     */
    @Builder.Default
    private Boolean enabledDynamicRegister = false;

    /**
     * 集群配置.
     */
    @NestedConfigurationProperty
    private Map<String, RabbitMultiClusterConfig> clusters;

    /**
     * 多集群Rabbit集群配置对象.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RabbitMultiClusterConfig {

        /**
         * 地址.
         */
        private String addresses;

        /**
         * 主机.
         */
        private String host;

        /**
         * 端口.
         */
        @Builder.Default
        private Integer port = 5672;

        /**
         * 用户名.
         */
        private String username;

        /**
         * 密码.
         */
        private String password;

        /**
         * 虚拟主机.
         */
        @Builder.Default
        private String virtualHost = "/";

        /**
         * 连接超时(ms)
         */
        @Builder.Default
        private Integer connectionTimeout = 3000;

        /**
         * 心跳间隔.
         */
        @Builder.Default
        private Integer requestedHeartbeat = 60;

        /**
         * channel 缓存
         */
        @Builder.Default
        private Integer channelCacheSize = 25;

        /**
         * 生产者配置.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private RabbitProducerConfig producer = new RabbitProducerConfig();

        /**
         * 消费者配置.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private RabbitConsumerConfig consumer = new RabbitConsumerConfig();
    }

    /**
     * 生产者配置.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RabbitProducerConfig {

        /**
         * 是否开启.
         */
        @Builder.Default
        private Boolean enabled = true;

        /**
         * 是否启用监控观察.
         */
        @Builder.Default
        private Boolean enabledObservation = Boolean.TRUE;

        /**
         * 线程池bean名称（对于DtpExecutor）.
         */
        private String threadPoolName;

        /**
         * 发送确认类型.
         */
        @Builder.Default
        private ConfirmType publisherConfirmType = ConfirmType.CORRELATED;

        /**
         * 是否开启发送确认返回.
         */
        @Builder.Default
        private Boolean publisherReturns = true;

        /**
         * 是否成功路由到
         */
        @Builder.Default
        private Boolean mandatory = true;

        /**
         * 是否启用重试
         */
        @Builder.Default
        private Boolean enableRetry = true;

        /**
         * 回复超时(ms)
         */
        @Builder.Default
        private Long replyTimeout = 3000L;

        /**
         * 重试次数
         */
        @Builder.Default
        private Integer retryAttempts = 3;

        /**
         * 初始重试间隔(ms)
         */
        @Builder.Default
        private Long initialInterval = 1000L;

        /**
         * 最大重试间隔(ms)
         */
        @Builder.Default
        private Long maxInterval = 10000L;
    }

    /**
     * 消费者配置.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RabbitConsumerConfig {

        /**
         * 是否开启.
         */
        @Builder.Default
        private Boolean enabled = true;

        /**
         * 是否启用监控观察.
         */
        @Builder.Default
        private Boolean enabledObservation = Boolean.TRUE;

        /**
         * 消费组（同一组（group）的多个 listener 会共享一个 ListenerContainer）.
         */
        private String group;

        /**
         * 监听线程池bean名称（对于DtpExecutor）.
         */
        private String threadPoolName;

        /**
         * 预取数量（一次推多少条消息给消费者）
         */
        @Builder.Default
        private Integer prefetch = 100;

        /**
         * 并发消费者
         */
        @Builder.Default
        private Integer concurrency = 1;

        /**
         * 最大并发
         */
        @Builder.Default
        private Integer maxConcurrency = 10;

        /**
         * Ack模式
         */
        @Builder.Default
        private AcknowledgeMode acknowledgeMode = AcknowledgeMode.MANUAL;

        /**
         * 是否批量处理.
         */
        @Builder.Default
        private Boolean enabledBatchListener = true;

        /**
         * 批量大小（消费者一次处理多少）
         */
        @Builder.Default
        private Integer batchSize = 20;

        /**
         * 是否启用重试
         */
        @Builder.Default
        private Boolean enableRetry = true;

        /**
         * 重试次数
         */
        @Builder.Default
        private Integer retryAttempts = 3;

        /**
         * 重试间隔(ms)
         */
        @Builder.Default
        private Long retryInterval = 1000L;

        /**
         * 接收超时(ms)
         */
        @Builder.Default
        private Long receiveTimeout = 1000L;

        /**
         * 空闲事件间隔(ms)
         */
        @Builder.Default
        private Long idleEventInterval = 60000L;

        /**
         * 队列不存在是否致命
         */
        @Builder.Default
        private Boolean missingQueuesFatal = false;

        /**
         * 优先级
         */
        @Builder.Default
        private Integer priority = 1;

        /**
         * 绑定关系配置.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private List<RabbitBindingConfig> bindings = new ArrayList<>(10);
    }

    /**
     * 绑定关系配置.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RabbitBindingConfig {

        /**
         * 是否自动创建
         */
        @Builder.Default
        private Boolean autoCreate = true;

        /**
         * 路由key（支持多个）
         */
        private List<String> routingKeys;

        /**
         * 交换机信息（必填）.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private Exchange exchange = new Exchange();

        /**
         * 队列信息（必填）.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private Queue queue = new Queue();

        /**
         * 消费组
         */
        private String group;

        /**
         * binding 参数（headers / x-match 等，可选）
         */
        private Map<String, Object> arguments;
    }

    /**
     * 交换机信息类.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Exchange {

        /**
         * 交换机类型（必填）.
         */
        @Builder.Default
        private RabbitExchangeTypeEnum type = RabbitExchangeTypeEnum.DIRECT;

        /**
         * 交换机名称（必填）.
         */
        private String name;

        /**
         * 是否持久化.
         */
        @Builder.Default
        private Boolean durable = true;

        /**
         * 当所有绑定队列均不在使用时，是否自动删除交换机.
         */
        @Builder.Default
        private Boolean autoDelete = false;

        /**
         * 交换机其他参数（可选）.
         */
        private Map<String, Object> arguments;
    }

    /**
     * 队列信息类.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Queue {

        /**
         * 队列名称（必填）.
         */
        private String name;

        /**
         * 是否持久化.
         */
        @Builder.Default
        private Boolean durable = true;

        /**
         * 是否具有排他性.
         */
        @Builder.Default
        private Boolean exclusive = false;

        /**
         * 当消费者均断开连接，是否自动删除队列.
         */
        @Builder.Default
        private Boolean autoDelete = false;

        /**
         * 推荐统一使用 arguments（包含 DLX / TTL 等）
         */
        private Map<String, Object> arguments;

        /**
         * 绑定死信队列的交换机名称（可选，也可通过arguments设置"x-dead-letter-exchange"）
         */
        private String deadLetterExchange;

        /**
         * 绑定死信队列的路由key（可选，也可通过arguments设置"x-dead-letter-routing-key"）
         */
        private String deadLetterRoutingKey;
    }

    /**
     * 获取生产者配置
     *
     * @param cluster 集群
     * @return
     */
    public RabbitProducerConfig getProducerConfig(String cluster) {
        RabbitMultiClusterConfig config = clusters.get(cluster);
        return config != null && config.getProducer() != null
            ? config.getProducer() : new RabbitProducerConfig();
    }

    /**
     * 获取消费者配置
     *
     * @param cluster 集群
     * @return
     */
    public RabbitConsumerConfig getConsumerConfig(String cluster) {
        RabbitMultiClusterConfig config = clusters.get(cluster);
        return config != null && config.getConsumer() != null
            ? config.getConsumer() : new RabbitConsumerConfig();
    }

    /**
     * 获取绑定关系配置
     *
     * @param cluster 集群
     * @return
     */
    public List<RabbitBindingConfig> listBindingConfig(String cluster) {
        final RabbitConsumerConfig config = getConsumerConfig(cluster);
        return config != null && CollUtil.isNotEmpty(config.getBindings())
            ? config.getBindings() : new ArrayList<>(10);
    }

    /**
     * 获取并发数
     *
     * @param cluster 集群
     * @return
     */
    public Integer getConcurrency(String cluster) {
        final RabbitConsumerConfig config = getConsumerConfig(cluster);
        return config != null && config.getConcurrency() != null
            ? config.getConcurrency() : 1;
    }

    /**
     * 获取消费组
     *
     * @param cluster 集群
     * @param group   组
     * @return
     */
    public String getGroup(String cluster, String group) {
        if (CharSequenceUtil.isNotBlank(group)) {
            return group;
        }

        final RabbitConsumerConfig config = getConsumerConfig(cluster);
        return config != null && CharSequenceUtil.isNotBlank(config.getGroup())
            ? config.getGroup()
            : RabbitConstant.RABBIT_DEFAULT_GROUP;
    }

    /**
     * 获取批量监听
     *
     * @param cluster 集群
     * @return
     */
    public boolean getEnabledBatchListener(String cluster) {
        final RabbitConsumerConfig config = getConsumerConfig(cluster);
        return config != null && config.getEnabledBatchListener() != null
            ? config.getEnabledBatchListener()
            : Boolean.TRUE;
    }

    /**
     * 是否启用监控观察（消费者）
     *
     * @param cluster 集群
     * @return
     */
    public Boolean getProducerEnabledObservation(String cluster) {
        final RabbitProducerConfig config = getProducerConfig(cluster);
        return config != null && config.getEnabledObservation();
    }

    /**
     * 是否启用监控观察（消费者）
     *
     * @param cluster 集群
     * @return
     */
    public Boolean getConsumerEnabledObservation(String cluster) {
        final RabbitConsumerConfig config = getConsumerConfig(cluster);
        return config != null && config.getEnabledObservation();
    }
}
