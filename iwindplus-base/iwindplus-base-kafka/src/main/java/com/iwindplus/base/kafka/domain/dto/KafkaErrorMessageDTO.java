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
 * kafka消费失败消息数据传输对象.
 *
 * @author zengdegui
 * @since 2026/07/20 12:25
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaErrorMessageDTO implements Serializable {

    /**
     * kafka集群
     */
    private String cluster;

    /**
     * 原topic
     */
    private String originalTopic;

    /**
     * 原partition
     */
    private Integer originalPartition;

    /**
     * 原offset
     */
    private Long originalOffset;

    /**
     * 消息key
     */
    private String key;

    /**
     * 当前重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetry;

    /**
     * 异常类型
     */
    private String exceptionType;

    /**
     * 异常信息
     */
    private String exceptionMessage;

    /**
     * 第一次失败时间
     */
    private Long firstFailedTime;

    /**
     * 最近失败时间
     */
    private Long lastFailedTime;
}
