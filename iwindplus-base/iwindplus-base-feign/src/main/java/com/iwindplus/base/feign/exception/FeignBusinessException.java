/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.feign.exception;

/**
 * Feign 业务类远程异常.
 *
 * <p>用于参数、认证、权限、资源不存在和业务冲突等错误，默认不应触发熔断。</p>
 *
 * @author zengdegui
 * @since 2026/8/25
 */
public class FeignBusinessException extends FeignRemoteException {

    /**
     * 构造业务类远程异常.
     *
     * @param bizCode      远程业务编码
     * @param message      异常消息
     * @param cause        原始异常
     * @param methodKey    Feign 方法标识
     * @param status       HTTP 状态码
     * @param responseBody 错误响应体摘要
     */
    public FeignBusinessException(
        String bizCode, String message, Throwable cause, String methodKey,
        int status, String responseBody) {
        super(bizCode, message, cause, methodKey, status, responseBody, false);
    }

}
