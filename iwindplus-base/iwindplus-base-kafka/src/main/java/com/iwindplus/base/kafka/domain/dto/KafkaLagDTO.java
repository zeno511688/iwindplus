/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.domain.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * kafka 消息堆积量.
 *
 * @author zengdegui
 * @since 2026/07/21 22:22
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaLagDTO implements Serializable {

    /**
     * 集群.
     */
    private String cluster;

    /**
     * 消费者.
     */
    private String group;

    /**
     * topic.
     */
    private String topic;

    /**
     * 分区.
     */
    private Integer partition;

    /**
     * 当前offset.
     */
    private Long currentOffset;

    /**
     * 结束offset.
     */
    private Long endOffset;

    /**
     * 堆积量.
     */
    private Long lag;
}
