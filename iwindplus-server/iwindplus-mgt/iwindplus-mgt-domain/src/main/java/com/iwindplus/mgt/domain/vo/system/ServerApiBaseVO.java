/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 服务API基础字段视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "服务API基础字段视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ServerApiBaseVO implements Serializable {

    /**
     * 请求方式.
     */
    @Schema(description = "请求方式")
    private String requestMethod;

    /**
     * API路径.
     */
    @Schema(description = "API路径")
    private String apiUrl;

    /**
     * 限流次数，每个时间窗口允许请求数量.
     */
    @Schema(description = "限流次数，每个时间窗口允许请求数量")
    private Long rate;
}