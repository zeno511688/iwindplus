/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.feign.exception;

/**
 * Feign 认证或权限异常.
 *
 * @author zengdegui
 * @since 2026/8/25
 */
public class FeignAuthenticationException extends FeignBusinessException {

    /**
     * 构造认证异常.
     *
     * @param bizCode          远程业务编码
     * @param message          异常消息
     * @param bizMessageParams 业务消息参数
     * @param cause            原始异常
     * @param methodKey        Feign 方法标识
     * @param status           HTTP 状态码
     * @param responseBody     错误响应体摘要
     */
    public FeignAuthenticationException(
        String bizCode, String message, Object[] bizMessageParams, Throwable cause, String methodKey,
        int status, String responseBody) {
        super(bizCode, message, bizMessageParams, cause, methodKey, status, responseBody);
    }

}
