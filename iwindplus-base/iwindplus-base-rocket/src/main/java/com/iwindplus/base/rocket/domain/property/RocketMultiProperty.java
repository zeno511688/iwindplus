/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.domain.property;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.rocket.domain.constant.RocketConstant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 多集群rocket配置.
 *
 * @author zengdegui
 * @since 2026/04/05 17:19
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "rocket.multi")
public class RocketMultiProperty {

    /**
     * 是否开启
     */
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 默认集群
     */
    private String defaultCluster;

    /**
     * 集群配置
     */
    @NestedConfigurationProperty
    private Map<String, RocketMultiClusterConfig> clusters;

    /**
     * 集群配置
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RocketMultiClusterConfig {

        /**
         * namesrv地址
         */
        private String nameServer;

        /**
         * 生产者
         */
        @Builder.Default
        @NestedConfigurationProperty
        private RocketProducerConfig producer = new RocketProducerConfig();

        /**
         * 消费者
         */
        @Builder.Default
        @NestedConfigurationProperty
        private RocketConsumerConfig consumer = new RocketConsumerConfig();

        /**
         * 绑定关系配置.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private List<RocketBindingConfig> bindings = new ArrayList<>(10);
    }

    /**
     * 生产者配置
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RocketProducerConfig {

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
         * 生产者组（可选）
         */
        private String group;

        /**
         * 线程池bean名称（对于DtpExecutor）.
         */
        private String threadPoolName;

        /**
         * 发送超时时间
         */
        @Builder.Default
        private Integer sendMsgTimeout = 1000;

        /**
         * 重试次数
         */
        @Builder.Default
        private Integer retryTimesWhenSendFailed = 3;

        /**
         * 异步发送失败重试次数
         */
        @Builder.Default
        private Integer retryTimesWhenSendAsyncFailed = 3;

        /**
         * 是否重试其他broker
         */
        @Builder.Default
        private Boolean retryAnotherBrokerWhenNotStoreOk = true;
    }

    /**
     * 消费者配置
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RocketConsumerConfig {

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
         * 消费组（可选）
         */
        private String group;

        /**
         * 消息模式
         */
        @Builder.Default
        private MessageModel messageModel = MessageModel.CLUSTERING;

        /**
         * 消费位置
         */
        @Builder.Default
        private ConsumeFromWhere consumeFromWhere = ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;

        /**
         * 消费超时时间（单位：分钟）
         */
        @Builder.Default
        private Integer consumeTimeout = 10;

        /**
         * 消费线程数最小值
         */
        @Builder.Default
        private Integer consumeThreadMin = 4;

        /**
         * 消费线程数最大值
         */
        @Builder.Default
        private Integer consumeThreadMax = 32;

        /**
         * 一次消费最大条数（批量）
         */
        @Builder.Default
        private Integer consumeMessageBatchMaxSize = 10;

        /**
         * 最大重试次数
         */
        @Builder.Default
        private Integer maxReconsumeTimes = 16;

        /**
         * 拉取消息超时（毫秒）
         */
        @Builder.Default
        private Long suspendCurrentQueueTimeMillis = 1000L;

        /**
         * 拉取批量大小
         */
        @Builder.Default
        private Integer pullBatchSize = 32;

        /**
         * 拉取间隔
         */
        @Builder.Default
        private Long pullInterval = 0L;

        /**
         * 是否顺序消费
         */
        @Builder.Default
        private Boolean orderly = false;
    }

    /**
     * 绑定关系配置
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RocketBindingConfig {

        /**
         * 主题名称.
         */
        private String topic;

        /**
         * Tag（支持 * 或 多个用 || 分隔）
         *
         * @return String
         */
        private String tag;

        /**
         * 消费组
         */
        private String group;
    }

    /**
     * 获取生产者配置
     *
     * @param cluster 集群
     * @return
     */
    public RocketProducerConfig getProducerConfig(String cluster) {
        RocketMultiClusterConfig config = clusters.get(cluster);
        return config != null && config.getProducer() != null
            ? config.getProducer() : new RocketProducerConfig();
    }

    /**
     * 获取消费者配置
     *
     * @param cluster 集群
     * @return
     */
    public RocketConsumerConfig getConsumerConfig(String cluster) {
        RocketMultiClusterConfig config = clusters.get(cluster);
        return config != null && config.getConsumer() != null
            ? config.getConsumer() : new RocketConsumerConfig();
    }

    /**
     * 获取生产者组
     *
     * @param cluster 集群
     * @return
     */
    public String getProducerGroup(String cluster) {
        final RocketProducerConfig config = getProducerConfig(cluster);

        return config != null && CharSequenceUtil.isNotBlank(config.getGroup())
            ? config.getGroup()
            : String.format("%s-%s-%s-producer", SpringUtil.getActiveProfile(), SpringUtil.getApplicationName(), cluster);
    }

    /**
     * 是否启用监控观察（消费者）
     *
     * @param cluster 集群
     * @return
     */
    public Boolean getProducerEnabledObservation(String cluster) {
        final RocketProducerConfig config = getProducerConfig(cluster);
        return config != null && config.getEnabledObservation();
    }

    /**
     * 是否启用监控观察（消费者）
     *
     * @param cluster 集群
     * @return
     */
    public Boolean getConsumerEnabledObservation(String cluster) {
        final RocketConsumerConfig config = getConsumerConfig(cluster);
        return config != null && config.getEnabledObservation();
    }

    /**
     * 获取消费者组
     *
     * @param cluster 集群
     * @param group   组
     * @return
     */
    public String getGroup(String cluster, String group) {
        if (CharSequenceUtil.isNotBlank(group)) {
            return group;
        }

        final RocketConsumerConfig config = getConsumerConfig(cluster);
        return config != null && CharSequenceUtil.isNotBlank(config.getGroup())
            ? config.getGroup()
            : RocketConstant.ROCKET_DEFAULT_GROUP;
    }

    /**
     * 获取绑定关系配置
     *
     * @param cluster 集群
     * @return
     */
    public List<RocketBindingConfig> listBindingConfig(String cluster) {
        RocketMultiClusterConfig config = clusters.get(cluster);
        return config != null && config.getBindings() != null
            ? config.getBindings() : new ArrayList<>(10);
    }

    /**
     * 获取Topic集合
     *
     * @param cluster 集群
     * @return
     */
    public List<String> listTopic(String cluster) {
        final List<RocketBindingConfig> bindings = listBindingConfig(cluster);

        return bindings
            .stream()
            .filter(Objects::nonNull)
            .map(RocketBindingConfig::getTopic)
            .filter(CharSequenceUtil::isNotBlank)
            .collect(Collectors.toList());
    }

    /**
     * 获取默认集群Topic集合
     *
     * @return
     */
    public List<String> listDefaultClusterTopic() {
        return listTopic(defaultCluster);
    }
}
