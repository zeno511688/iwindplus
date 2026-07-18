/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.disruptor.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2018/12/27
 */
public final class DisruptorConstant {

    private DisruptorConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 消费者.
     */
    public static final String DISRUPTOR_CONSUMER = "disruptor.consumer";

    /**
     * Handler名称.
     */
    public static final String HANDLER = "handler";

    /**
     * 序列号.
     */
    public static final String SEQUENCE = "sequence";

    /**
     * 数据来源.
     */
    public static final String SOURCE = "source";

    /**
     * 数据去向.
     */
    public static final String DESTINATION = "destination";
}
