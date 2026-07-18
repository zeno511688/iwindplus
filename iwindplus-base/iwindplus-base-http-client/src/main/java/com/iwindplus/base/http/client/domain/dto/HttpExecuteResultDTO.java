/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.domain.dto;


import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;

/**
 * HTTP 执行结果
 *
 * <p>
 * 不抛异常，只描述结果，异常由上层模板统一处理
 * </p>
 *
 * @author zengdegui
 * @since 2026/01/19 23:19
 */
public record HttpExecuteResultDTO(int status, String body, Object error) {

    /**
     * 成功.
     *
     * @param status 状态码
     * @param body   内容
     * @return 结果
     */
    public static HttpExecuteResultDTO success(int status, String body) {
        return new HttpExecuteResultDTO(status, body, null);
    }

    /**
     * 失败.
     *
     * @param status 状态码
     * @param error  错误
     * @return 结果
     */
    public static HttpExecuteResultDTO error(int status, Object error) {
        return new HttpExecuteResultDTO(status, null, error);
    }

    /**
     * 失败.
     *
     * @param error 错误
     * @return 结果
     */
    public static HttpExecuteResultDTO error(Object error) {
        return new HttpExecuteResultDTO(NumberConstant.NUMBER_NEGATIVE_ONE, null, error);
    }
}
