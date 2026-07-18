/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2020/11/8
 */
public class DtxConstant {

    private DtxConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 分布式服务名.
     */
    public static final String DTX_SERVER_NAME = "iwindplus-dtx";

    /**
     * 分布式服务客户端扫描包名.
     */
    public static final String DTX_CLIENT_SCAN_BASE_PACKAGE = "com.iwindplus.dtx.client";
}
