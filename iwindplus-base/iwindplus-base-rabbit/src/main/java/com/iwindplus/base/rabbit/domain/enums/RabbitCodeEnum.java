/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.base.rabbit.domain.enums;

import com.iwindplus.base.domain.exception.CommonException;
import lombok.Getter;

/**
 * 业务编码枚举.
 *
 * @author zengdegui
 * @since 2020/6/13
 */
@Getter
public enum RabbitCodeEnum implements CommonException {

    /**
     * rabbit集群不存在.
     */
    RABBIT_CLUSTER_NOT_EXIST("rabbit_cluster_not_exist", "rabbit集群不存在"),

    /**
     * rabbit连接器不存在.
     */
    RABBIT_CONNECTION_NOT_EXIST("rabbit_connection_not_exist", "rabbit连接器不存在"),

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
    RabbitCodeEnum(final String bizCode, final String bizMessage) {
        this.bizCode = bizCode;
        this.bizMessage = bizMessage;
    }
}
