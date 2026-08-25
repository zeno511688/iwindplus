/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.feign.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2018/12/27
 */
public final class FeignConstant {

    private FeignConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 默认错误响应体最大读取字节数.
     */
    public static final int DEFAULT_MAX_RESPONSE_BODY_SIZE = 16 * 1024;

    /**
     * 错误响应体读取缓冲区大小.
     */
    public static final int RESPONSE_BODY_BUFFER_SIZE = 1024;
}
