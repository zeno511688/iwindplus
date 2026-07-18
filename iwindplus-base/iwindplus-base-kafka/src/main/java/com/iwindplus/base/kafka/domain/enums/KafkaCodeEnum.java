/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.base.kafka.domain.enums;

import com.iwindplus.base.domain.exception.CommonException;
import lombok.Getter;

/**
 * 业务编码枚举.
 *
 * @author zengdegui
 * @since 2020/6/13
 */
@Getter
public enum KafkaCodeEnum implements CommonException {
    /**
     * kafka集群不存在.
     */
    KAFKA_CLUSTER_NOT_EXIST("kafka_cluster_not_exist", "kafka集群不存在"),

    /**
     * kafka监听器不存在.
     */
    KAFKA_LISTENER_NOT_EXIST("kafka_listener_not_exist", "kafka监听器不存在"),

    ;
    /**
     * 业务编码.
     */
    private final String bizCode;

    /**
     * 业务信息.
     */
    private final String bizMessage;

    /**
     * 构造方法.
     *
     * @param bizCode    业务编码
     * @param bizMessage 业务信息
     */
    KafkaCodeEnum(final String bizCode, final String bizMessage) {
        this.bizCode = bizCode;
        this.bizMessage = bizMessage;
    }
}
