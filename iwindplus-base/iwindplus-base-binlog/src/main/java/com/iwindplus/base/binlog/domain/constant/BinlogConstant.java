/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.binlog.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * binlog常数.
 *
 * @author zengdegui
 * @since 2025/11/28 22:45
 */
public class BinlogConstant {

    private BinlogConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 操作.
     */
    public static final String OP = "op";

    /**
     * 处理时间（毫秒）.
     */
    public static final String TS_MS = "ts_ms";

    /**
     * 处理时间（微秒）.
     */
    public static final String TS_US = "ts_us";

    /**
     * 处理时间（纳秒）.
     */
    public static final String TS_NS = "ts_ns";

    /**
     * 事务.
     */
    public static final String TRANSACTION = "transaction";

    /**
     * 载荷.
     */
    public static final String PAYLOAD = "payload";

    /**
     * 元数据.
     */
    public static final String SOURCE = "source";

    /**
     * 操作前数据.
     */
    public static final String BEFORE = "before";

    /**
     * 操作后数据.
     */
    public static final String AFTER = "after";
}
