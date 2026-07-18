/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.domain.dto;

import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Kafka 消息数据传输对象.
 *
 * @author zengdegui
 * @since 2026/03/26 00:59
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaMessageDTO implements Serializable {

    /**
     * 集群
     */
    private String cluster;

    /**
     * 主题名称
     */
    private String topic;

    /**
     * key
     */
    private String key;

    /**
     * 头
     */
    private Map<String, Object> headers;

    /**
     * 消息体
     */
    private String message;
}
