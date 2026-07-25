/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.monitor.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 监控指标常量.
 *
 * @author zengdegui
 */
public final class MonitorConstant {

    private MonitorConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 容量.
     */
    public static final String CAPACITY = "capacity";

    /**
     * 当前使用量.
     */
    public static final String SIZE = "size";

    /**
     * 使用量.
     */
    public static final String USAGE = "usage";

    /**
     * 使用率.
     */
    public static final String USAGE_PERCENT = "usage_percent";

    /**
     * 成功数量.
     */
    public static final String CONSUME_SUCCESS = "consume.success";

    /**
     * 失败数量.
     */
    public static final String CONSUME_ERROR = "consume.error";

    /**
     * 处理耗时.
     */
    public static final String CONSUME_PROCESS_TIME = "consume.process.time";

    /**
     * 名称.
     */
    public static final String NAME = "name";

    /**
     * 类型.
     */
    public static final String TYPE = "type";

    /**
     * 来源.
     */
    public static final String SOURCE = "source";

    /**
     * 目标.
     */
    public static final String DESTINATION = "destination";
}