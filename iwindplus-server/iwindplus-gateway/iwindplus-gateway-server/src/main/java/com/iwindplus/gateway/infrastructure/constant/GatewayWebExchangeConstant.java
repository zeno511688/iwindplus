/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.infrastructure.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 网关网络交换上下文常数.
 *
 * @author zengdegui
 * @since 2026/09/14 23:59
 */
public class GatewayWebExchangeConstant {

    private GatewayWebExchangeConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 请求开始时间.
     */
    public static final String REQUEST_TIME = "requestTime";

    /**
     * 白名单标记.
     */
    public static final String WHITED_FLAG = "whitedFlag";

    /**
     * 用户信息.
     */
    public static final String USER_INFO = "userInfo";
}
