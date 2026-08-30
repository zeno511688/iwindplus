/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.comsumer.server.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * binlog常数.
 *
 * @author zengdegui
 * @since 2025/11/28 22:45
 */
public class BinlogConsumerConstant {

    private BinlogConsumerConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 线程池bean名称.
     */
    public static final String THREAD_POOL_BEAN_NAME = "binlogThreadPool";
}
