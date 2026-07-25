/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.disruptor.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.monitor.domain.constant.MonitorConstant;

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
    public static final String DISRUPTOR = "disruptor";

    /**
     * Handler名称.
     */
    public static final String HANDLER = "handler";

    /**
     * 数据来源.
     */
    public static final String SOURCE = "source";

    /**
     * 数据去向.
     */
    public static final String DESTINATION = "destination";

    /**
     * Disruptor监控常量.
     */
    public final class DisruptorMonitorConstant {

        /**
         * ringBuffer.
         */
        public static final String RING_BUFFER = ".ringbuffer.";

        /**
         * 总容量.
         */
        public static final String RING_BUFFER_CAPACITY =
            DISRUPTOR + RING_BUFFER + MonitorConstant.CAPACITY;

        /**
         * 剩余容量.
         */
        public static final String RING_BUFFER_REMAINING =
            DISRUPTOR + RING_BUFFER + MonitorConstant.REMAINING;

        /**
         * 使用量.
         */
        public static final String RING_BUFFER_USAGE =
            DISRUPTOR + RING_BUFFER + MonitorConstant.USAGE;

        /**
         * 使用率.
         */
        public static final String RING_BUFFER_USAGE_PERCENT =
            DISRUPTOR + RING_BUFFER + MonitorConstant.USAGE_PERCENT;
    }
}
