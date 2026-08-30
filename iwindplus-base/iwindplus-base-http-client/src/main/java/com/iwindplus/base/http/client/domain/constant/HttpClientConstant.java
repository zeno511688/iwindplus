/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2026/05/17 18:19
 */
public class HttpClientConstant {

    private HttpClientConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 默认获取应用凭证前缀.
     */
    public static final String DEFAULT_GET_APP_CERT_PREFIX = "lb://iwindplus-mgt";

    /**
     * 线程池bean名称.
     */
    public static final String THREAD_POOL_BEAN_NAME = "httpClientThreadPool";
}
