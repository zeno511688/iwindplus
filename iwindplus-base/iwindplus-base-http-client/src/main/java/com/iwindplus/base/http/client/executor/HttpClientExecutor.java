/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.executor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.http.client.domain.dto.HttpRequestSpecDTO;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import org.springframework.http.HttpMethod;
import org.springframework.web.multipart.MultipartFile;

/**
 * HTTP 客户端执行器统一抽象.
 *
 * @author zengdegui
 * @since 2026/01/19
 */
public interface HttpClientExecutor {

    /**
     * 获取当前 HTTP 客户端类型.
     *
     * @return HttpClientTypeEnum
     */
    HttpClientTypeEnum getClientType();

    /**
     * 同步 GET 请求.
     *
     * @param url          请求地址（完整 URL 或相对路径）
     * @param query        查询参数（可为空）
     * @param headers      请求头（可为空）
     * @param responseType 响应反序列化目标类型
     * @param <T>          泛型
     * @return T
     */
    default <T> T get(
        String url,
        Map<String, ?> query,
        Map<String, String> headers,
        Class<T> responseType) {
        return exchange(
            HttpRequestSpecDTO.query(HttpMethod.GET.name(), url, headers, query),
            responseType
        );
    }

    /**
     * 同步 GET 请求（支持泛型响应）.
     *
     * @param url           请求地址
     * @param query         查询参数（可为空）
     * @param headers       请求头（可为空）
     * @param typeReference 响应反序列化类型引用（如 {@code List<T>}）（如 {@code List<T>}）
     * @param <T>           泛型
     * @return T
     */
    default <T> T get(
        String url,
        Map<String, ?> query,
        Map<String, String> headers,
        TypeReference<T> typeReference) {
        return exchange(
            HttpRequestSpecDTO.query(HttpMethod.GET.name(), url, headers, query),
            typeReference
        );
    }

    /**
     * 异步 GET 请求.
     *
     * @param url          请求地址
     * @param query        查询参数（可为空）
     * @param headers      请求头（可为空）
     * @param responseType 响应反序列化目标类型
     * @param <T>          泛型
     * @return CompletionStage<T>
     */
    default <T> CompletionStage<T> getAsync(
        String url,
        Map<String, ?> query,
        Map<String, String> headers,
        Class<T> responseType) {
        return exchangeAsync(
            HttpRequestSpecDTO.query(HttpMethod.GET.name(), url, headers, query),
            responseType
        );
    }

    /**
     * 异步 GET 请求（支持泛型响应）.
     *
     * @param url           请求地址
     * @param query         查询参数（可为空）
     * @param headers       请求头（可为空）
     * @param typeReference 响应反序列化类型引用（如 {@code List<T>}）（如 {@code List<T>}）
     * @param <T>           泛型
     * @return CompletionStage<T>
     */
    default <T> CompletionStage<T> getAsync(
        String url,
        Map<String, ?> query,
        Map<String, String> headers,
        TypeReference<T> typeReference) {
        return exchangeAsync(
            HttpRequestSpecDTO.query(HttpMethod.GET.name(), url, headers, query),
            typeReference
        );
    }

    /**
     * 同步 POST 请求（JSON 请求体）.
     *
     * @param url          请求地址
     * @param body         请求体对象
     * @param headers      请求头（可为空）
     * @param responseType 响应反序列化目标类型
     * @param <T>          泛型
     * @return T
     */
    default <T> T post(
        String url,
        Object body,
        Map<String, String> headers,
        Class<T> responseType) {
        return exchange(
            HttpRequestSpecDTO.json(HttpMethod.POST.name(), url, headers, body),
            responseType
        );
    }

    /**
     * 同步 POST 请求（JSON 请求体）.
     *
     * @param url           请求地址
     * @param body          请求体对象
     * @param headers       请求头（可为空）
     * @param typeReference 响应反序列化类型引用（如 {@code List<T>}）
     * @param <T>           泛型
     * @return T
     */
    default <T> T post(
        String url,
        Object body,
        Map<String, String> headers,
        TypeReference<T> typeReference) {
        return exchange(
            HttpRequestSpecDTO.json(HttpMethod.POST.name(), url, headers, body),
            typeReference
        );
    }

    /**
     * 异步 POST 请求（JSON 请求体）.
     *
     * @param url          请求地址
     * @param body         请求体对象
     * @param headers      请求头（可为空）
     * @param responseType 响应反序列化目标类型
     * @param <T>          泛型
     * @return CompletionStage<T>
     */
    default <T> CompletionStage<T> postAsync(
        String url,
        Object body,
        Map<String, String> headers,
        Class<T> responseType) {
        return exchangeAsync(
            HttpRequestSpecDTO.json(HttpMethod.POST.name(), url, headers, body),
            responseType
        );
    }

    /**
     * 异步 POST 请求（JSON 请求体）.
     *
     * @param url           请求地址
     * @param body          请求体对象
     * @param headers       请求头（可为空）
     * @param typeReference 响应反序列化类型引用（如 {@code List<T>}）
     * @param <T>           泛型
     * @return CompletionStage<T>
     */
    default <T> CompletionStage<T> postAsync(
        String url,
        Object body,
        Map<String, String> headers,
        TypeReference<T> typeReference) {
        return exchangeAsync(
            HttpRequestSpecDTO.json(HttpMethod.POST.name(), url, headers, body),
            typeReference
        );
    }

    /**
     * 同步 POST 请求（表单 / 文件上传）.
     *
     * @param url          请求地址
     * @param form         表单字段（可为空）
     * @param files        上传文件列表（可为空）
     * @param headers      请求头（可为空）
     * @param responseType 响应反序列化目标类型
     * @param <T>          泛型
     * @return T
     */
    default <T> T post(
        String url,
        Map<String, ?> form,
        List<MultipartFile> files,
        Map<String, String> headers,
        Class<T> responseType) {
        return exchange(
            HttpRequestSpecDTO.multipart(HttpMethod.POST.name(), url, headers, form, files),
            responseType
        );
    }

    /**
     * 同步 POST 请求（表单 / 文件上传）.
     *
     * @param url           请求地址
     * @param form          表单字段（可为空）
     * @param files         上传文件列表（可为空）
     * @param headers       请求头（可为空）
     * @param typeReference 响应反序列化类型引用（如 {@code List<T>}）（如 {@code List<T>}）
     * @param <T>           泛型
     * @return T
     */
    default <T> T post(
        String url,
        Map<String, ?> form,
        List<MultipartFile> files,
        Map<String, String> headers,
        TypeReference<T> typeReference) {
        return exchange(
            HttpRequestSpecDTO.multipart(HttpMethod.POST.name(), url, headers, form, files),
            typeReference
        );
    }

    /**
     * 异步 POST 请求（表单 / 文件上传）.
     *
     * @param url          请求地址
     * @param form         表单字段（可为空）
     * @param files        上传文件列表（可为空）
     * @param headers      请求头（可为空）
     * @param responseType 响应反序列化目标类型
     * @param <T>          泛型
     * @return CompletionStage<T>
     */
    default <T> CompletionStage<T> postAsync(
        String url,
        Map<String, ?> form,
        List<MultipartFile> files,
        Map<String, String> headers,
        Class<T> responseType) {
        return exchangeAsync(
            HttpRequestSpecDTO.multipart(HttpMethod.POST.name(), url, headers, form, files),
            responseType
        );
    }

    /**
     * 异步 POST 请求（表单 / 文件上传）.
     *
     * @param url           请求地址
     * @param form          表单字段（可为空）
     * @param files         上传文件列表（可为空）
     * @param headers       请求头（可为空）
     * @param typeReference 响应反序列化类型引用（如 {@code List<T>}）
     * @param <T>           泛型
     * @return CompletionStage<T>
     */
    default <T> CompletionStage<T> postAsync(
        String url,
        Map<String, ?> form,
        List<MultipartFile> files,
        Map<String, String> headers,
        TypeReference<T> typeReference) {
        return exchangeAsync(
            HttpRequestSpecDTO.multipart(HttpMethod.POST.name(), url, headers, form, files),
            typeReference
        );
    }

    /**
     * 同步 PUT 请求（JSON 请求体）.
     *
     * @param url          请求地址
     * @param body         请求体对象
     * @param headers      请求头（可为空）
     * @param responseType 响应反序列化目标类型
     * @param <T>          泛型
     * @return T
     */
    default <T> T put(
        String url,
        Object body,
        Map<String, String> headers,
        Class<T> responseType) {
        return exchange(
            HttpRequestSpecDTO.json(HttpMethod.PUT.name(), url, headers, body),
            responseType
        );
    }

    /**
     * 同步 PUT 请求（JSON 请求体，支持泛型响应）.
     *
     * @param url           请求地址
     * @param body          请求体对象
     * @param headers       请求头（可为空）
     * @param typeReference 响应反序列化类型引用（如 {@code List<T>}）
     * @param <T>           泛型
     * @return T
     */
    default <T> T put(
        String url,
        Object body,
        Map<String, String> headers,
        TypeReference<T> typeReference) {
        return exchange(
            HttpRequestSpecDTO.json(HttpMethod.PUT.name(), url, headers, body),
            typeReference
        );
    }

    /**
     * 异步 PUT 请求（JSON 请求体）.
     *
     * @param url          请求地址
     * @param body         请求体对象
     * @param headers      请求头（可为空）
     * @param responseType 响应反序列化目标类型
     * @param <T>          泛型
     * @return CompletionStage<T>
     */
    default <T> CompletionStage<T> putAsync(
        String url,
        Object body,
        Map<String, String> headers,
        Class<T> responseType) {
        return exchangeAsync(
            HttpRequestSpecDTO.json(HttpMethod.PUT.name(), url, headers, body),
            responseType
        );
    }

    /**
     * 异步 PUT 请求（JSON 请求体，支持泛型响应）.
     *
     * @param url           请求地址
     * @param body          请求体对象
     * @param headers       请求头（可为空）
     * @param typeReference 响应反序列化类型引用（如 {@code List<T>}）
     * @param <T>           泛型
     * @return CompletionStage<T>
     */
    default <T> CompletionStage<T> putAsync(
        String url,
        Object body,
        Map<String, String> headers,
        TypeReference<T> typeReference) {
        return exchangeAsync(
            HttpRequestSpecDTO.json(HttpMethod.PUT.name(), url, headers, body),
            typeReference
        );
    }

    /**
     * 同步 DELETE 请求.
     *
     * @param url          请求地址
     * @param query        查询参数（可为空）
     * @param headers      请求头（可为空）
     * @param responseType 响应反序列化目标类型
     * @param <T>          泛型
     * @return T
     */
    default <T> T delete(
        String url,
        Map<String, ?> query,
        Map<String, String> headers,
        Class<T> responseType) {
        return exchange(
            HttpRequestSpecDTO.query(HttpMethod.DELETE.name(), url, headers, query),
            responseType
        );
    }

    /**
     * 异步 DELETE 请求.
     *
     * @param url          请求地址
     * @param query        查询参数（可为空）
     * @param headers      请求头（可为空）
     * @param responseType 响应反序列化目标类型
     * @param <T>          泛型
     * @return CompletionStage<T>
     */
    default <T> CompletionStage<T> deleteAsync(
        String url,
        Map<String, ?> query,
        Map<String, String> headers,
        Class<T> responseType) {
        return exchangeAsync(
            HttpRequestSpecDTO.query(HttpMethod.DELETE.name(), url, headers, query),
            responseType
        );
    }

    /**
     * 同步通用执行入口.
     *
     * @param request      HTTP 请求描述对象
     * @param responseType 响应反序列化目标类型
     * @param <T>          泛型
     * @return T
     */
    <T> T exchange(
        HttpRequestSpecDTO request,
        Class<T> responseType
    );

    /**
     * 同步通用执行入口（泛型响应）.
     *
     * @param request       HTTP 请求描述对象
     * @param typeReference 响应反序列化类型引用（如 {@code List<T>}）
     * @param <T>           泛型
     * @return T
     */
    <T> T exchange(
        HttpRequestSpecDTO request,
        TypeReference<T> typeReference
    );

    /**
     * 异步通用执行入口.
     *
     * @param request      HTTP 请求描述对象
     * @param responseType 响应反序列化目标类型
     * @param <T>          泛型
     * @return CompletionStage<T>
     */
    <T> CompletionStage<T> exchangeAsync(
        HttpRequestSpecDTO request,
        Class<T> responseType
    );

    /**
     * 异步通用执行入口（泛型响应）.
     *
     * @param request       HTTP 请求描述对象
     * @param typeReference 响应反序列化类型引用（如 {@code List<T>}）
     * @param <T>           泛型
     * @return CompletionStage<T>
     */
    <T> CompletionStage<T> exchangeAsync(
        HttpRequestSpecDTO request,
        TypeReference<T> typeReference
    );

}
