/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * rocket常数.
 *
 * @author zengdegui
 * @since 2024/06/10 20:03
 */
public final class RocketConstant {

    private RocketConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 默认的消费者组.
     */
    public static final String ROCKET_DEFAULT_GROUP = "default-group";

    /**
     * 生产者.
     */
    public static final String ROCKET_PRODUCER = "rocketmq.producer";

    /**
     * 消费者.
     */
    public static final String ROCKET_CONSUMER = "rocketmq.consumer";

    /**
     * 集群
     */
    public static final String CLUSTER = "cluster";

    /**
     * 主题
     */
    public static final String TOPIC = "topic";

    /**
     * 消费组
     */
    public static final String GROUP = "group";

    /**
     * 标签
     */
    public static final String TAG = "tag";
}
