/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.feign.exception;

/**
 * Feign 远程限流异常.
 *
 * <p>429 表明对端正在保护自身，默认不应计入对端服务故障熔断。</p>
 *
 * @author zengdegui
 * @since 2026/8/25
 */
public class FeignRateLimitException extends FeignBusinessException {

    /**
     * 构造限流异常.
     *
     * @param bizCode      远程业务编码
     * @param message      异常消息
     * @param cause        原始异常
     * @param methodKey    Feign 方法标识
     * @param status       HTTP 状态码
     * @param responseBody 错误响应体摘要
     */
    public FeignRateLimitException(
        String bizCode, String message, Throwable cause, String methodKey,
        int status, String responseBody) {
        super(bizCode, message, cause, methodKey, status, responseBody);
    }

}
