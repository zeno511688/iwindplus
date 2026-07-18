/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */
package com.iwindplus.base.http.client.template;

import com.iwindplus.base.http.client.domain.dto.HttpExecuteResultDTO;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 * HTTP 执行统一模板（同步 / 异步）.
 *
 * @author zengdegui
 * @since 2026/01/17
 */
public interface HttpExecuteTemplate {

    /**
     * 执行 HTTP 请求（同步）
     *
     * @param client   客户端名称
     * @param method   HTTP 方法
     * @param url      请求 URL
     * @param supplier 执行方法
     * @return 结果
     */
    HttpExecuteResultDTO execute(
        String client,
        String method,
        String url,
        Supplier<HttpExecuteResultDTO> supplier);

    /**
     * 执行 HTTP 请求（异步）
     *
     * @param client   客户端名称
     * @param method   HTTP 方法
     * @param url      请求 URL
     * @param supplier 执行方法
     * @return 响应
     */
    CompletionStage<HttpExecuteResultDTO> executeAsync(
        String client,
        String method,
        String url,
        Supplier<CompletionStage<HttpExecuteResultDTO>> supplier);

}
