/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.domain.dto;

import com.iwindplus.base.http.client.domain.enums.HttpBodyTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

/**
 * HTTP请求数据传输对象.
 *
 * @author zengdegui
 * @since 2026/01/19 23:21
 */
@Schema(description = "HTTP请求数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class HttpRequestSpecDTO implements Serializable {

    /**
     * HTTP 方法
     */
    private String method;

    /**
     * 完整 URL
     */
    private String url;

    /**
     * Query 参数
     */
    private Map<String, ?> query;

    /**
     * Header
     */
    private Map<String, String> headers;

    /**
     * 请求体类型
     */
    private HttpBodyTypeEnum bodyType;

    /**
     * 表单参数
     */
    private Map<String, ?> form;

    /**
     * Multipart 文件
     */
    private List<MultipartFile> files;

    /**
     * JSON 请求体
     */
    private Object body;

    /**
     * 拼接参数 请求快捷构造
     */
    public static HttpRequestSpecDTO query(
        String method, String url,
        Map<String, String> headers, Map<String, ?> query) {
        return new HttpRequestSpecDTO(
            method, url,
            query, headers,
            HttpBodyTypeEnum.NONE,
            null, null,
            null
        );
    }

    /**
     * JSON 请求快捷构造
     */
    public static HttpRequestSpecDTO json(
        String method, String url,
        Map<String, String> headers, Object body) {
        return new HttpRequestSpecDTO(
            method, url,
            null, headers,
            HttpBodyTypeEnum.JSON,
            null, null,
            body
        );
    }

    /**
     * 表单请求
     */
    public static HttpRequestSpecDTO form(
        String method, String url,
        Map<String, String> headers, Map<String, ?> form) {
        return new HttpRequestSpecDTO(
            method, url,
            null, headers,
            HttpBodyTypeEnum.FORM,
            form, null,
            null
        );
    }

    public static HttpRequestSpecDTO multipart(
        String method, String url, Map<String, String> headers,
        Map<String, ?> form, List<MultipartFile> files) {
        return new HttpRequestSpecDTO(
            method, url,
            null, headers,
            HttpBodyTypeEnum.MULTIPART,
            form, files,
            null
        );
    }
}