/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * API签名生成数据传输对象.
 *
 * @author zengdegui
 * @since 2025/04/24 00:00
 */
@Schema(description = "API签名生成数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ApiSignGenerateDTO implements Serializable {

    /**
     * 应用（可选）.
     */
    @Schema(description = "应用")
    private String application;

    /**
     * 密钥.
     */
    @Schema(description = "密钥")
    private String secretKey;

    /**
     * 访问key.
     */
    @Schema(description = "访问key")
    private String accessKey;

    /**
     * 时间戳.
     */
    @Schema(description = "时间戳")
    private String timestamp;

    /**
     * nonce随机数（长度 >= 10）.
     */
    @Schema(description = "nonce随机数")
    private String nonce;

    /**
     * 请求路径.
     */
    @Schema(description = "请求路径")
    private String path;

    /**
     * 请求方式.
     */
    @Schema(description = "请求方式")
    private String method;

    /**
     * 请求参数.
     */
    @Schema(description = "请求参数")
    private Map<String, Object> params;
}
