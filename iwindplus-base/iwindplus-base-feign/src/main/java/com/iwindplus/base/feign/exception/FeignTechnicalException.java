/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.feign.exception;

/**
 * Feign 技术类远程异常.
 *
 * <p>用于网络、超时、网关和远程服务故障，默认允许 CircuitBreaker 统计。</p>
 *
 * @author zengdegui
 * @since 2026/8/25
 */
public class FeignTechnicalException extends FeignRemoteException {

    /**
     * 构造技术类远程异常.
     *
     * @param bizCode      技术错误编码
     * @param message      异常消息
     * @param cause        原始异常
     * @param methodKey    Feign 方法标识
     * @param status       HTTP 状态码
     * @param responseBody 错误响应体摘要
     * @param retryable    是否建议重试
     */
    public FeignTechnicalException(
        String bizCode, String message, Throwable cause, String methodKey, int status,
        String responseBody, boolean retryable) {
        super(bizCode, message, cause, methodKey, status, responseBody, retryable);
    }

}
