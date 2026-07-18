/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2020/11/8
 */
public class LogConstant {
    private LogConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 日志服务名.
     */
    public static final String LOG_SERVER_NAME = "iwindplus-log";

    /**
     * 日志服务客户端扫描包名.
     */
    public static final String LOG_CLIENT_SCAN_BASE_PACKAGE = "com.iwindplus.log.client";
}
