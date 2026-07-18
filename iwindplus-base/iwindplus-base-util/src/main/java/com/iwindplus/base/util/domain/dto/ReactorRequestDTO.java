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
 * Reactor请求数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "Reactor请求数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ReactorRequestDTO implements Serializable {

    /**
     * 查询参数.
     */
    @Schema(description = "查询参数")
    private Map<String, String> queryParams;

    /**
     * 请求头.
     */
    @Schema(description = "请求头")
    private Map<String, String> requestHeaders;

    /**
     * 请求体.
     */
    @Schema(description = "请求体")
    private String requestBody;
}
