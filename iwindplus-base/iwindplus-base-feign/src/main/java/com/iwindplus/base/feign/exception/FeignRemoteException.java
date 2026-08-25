/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.feign.exception;

import com.iwindplus.base.domain.exception.BizException;
import lombok.Getter;

/**
 * Feign 远程调用异常基类.
 *
 * @author zengdegui
 * @since 2026/8/25
 */
@Getter
public class FeignRemoteException extends BizException {

    /**
     * Feign 方法标识.
     */
    private final String methodKey;

    /**
     * HTTP 状态码.
     */
    private final int status;

    /**
     * 错误响应体摘要.
     */
    private final String responseBody;

    /**
     * 是否建议重试.
     */
    private final boolean retryable;

    /**
     * 构造远程调用异常.
     *
     * @param bizCode      业务编码
     * @param message      异常消息
     * @param cause        原始异常
     * @param methodKey    Feign 方法标识
     * @param status       HTTP 状态码
     * @param responseBody 错误响应体摘要
     * @param retryable    是否建议重试
     */
    public FeignRemoteException(
        String bizCode, String message, Throwable cause, String methodKey,
        int status, String responseBody, boolean retryable) {
        super(bizCode, message);
        this.methodKey = methodKey;
        this.status = status;
        this.responseBody = responseBody;
        this.retryable = retryable;
        if (cause != null) {
            initCause(cause);
        }
    }

}
