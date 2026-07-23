/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.domain.dto;

import java.io.Serializable;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;

/**
 * Kafka消费者信息.
 *
 * @author zengdegui
 * @since 2026/07/21 22:01
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaConsumerInfoDTO implements Serializable {

    /**
     * Kafka集群.
     */
    private String cluster;

    /**
     * 消费组.
     */
    private String group;

    /**
     * topics.
     */
    private Set<String> topics;

    /**
     * listener id.
     */
    private String listenerId;

    /**
     * kafka client id.
     */
    private String clientId;

    /**
     * 最大并发.
     */
    private Integer maxConcurrency;

    /**
     * 当前运行并发.
     */
    private Integer currentConcurrency;

    /**
     * 监听器.
     */
    private AbstractMessageListenerContainer<String, Object> container;
}
