/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.base.rocket.domain.enums;

import com.iwindplus.base.domain.exception.CommonException;
import lombok.Getter;

/**
 * 业务编码枚举.
 *
 * @author zengdegui
 * @since 2020/6/13
 */
@Getter
public enum RocketCodeEnum implements CommonException {

    /**
     * rocket集群不存在.
     */
    ROCKET_CLUSTER_NOT_EXIST("rocket_cluster_not_exist", "rocket集群不存在"),

    /**
     * 同一个Consumer不能混用顺序和并发消费.
     */
    ROCKET_CONSUMER_MIXED_CONSUMPTION_MODE("rocket_consumer_mixed_consumption_mode", "同一个Consumer不能混用顺序和并发消费")
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
    RocketCodeEnum(final String bizCode, final String bizMessage) {
        this.bizCode = bizCode;
        this.bizMessage = bizMessage;
    }
}
