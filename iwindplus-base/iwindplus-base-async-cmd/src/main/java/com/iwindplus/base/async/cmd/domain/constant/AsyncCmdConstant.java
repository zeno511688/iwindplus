/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2020/11/8
 */
public class AsyncCmdConstant {

    private AsyncCmdConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 异步命令mapper扫描包名.
     */
    public static final String ASYNC_CMD_MAPPER_SCAN_BASE_PACKAGE = "com.iwindplus.base.async.cmd.dal.mapper";

    /**
     * 异步命令bean扫描包名.
     */
    public static final String ASYNC_CMD_COMPONENT_SCAN_BASE_PACKAGE = "com.iwindplus.base.async.cmd";
}
