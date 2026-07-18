/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.system;

import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 服务API分页视图对象.
 *
 * @author zengdegui
 * @since 2020/4/14
 */
@Schema(description = "服务API分页视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ServerApiPageVO extends DbVersionBaseVO {

    /**
     * 应用名称.
     */
    @Schema(description = "应用名称")
    private String appName;

    /**
     * 控制器名称.
     */
    @Schema(description = "控制器名称")
    private String controllerName;

    /**
     * 请求方式.
     */
    @Schema(description = "请求方式")
    private String requestMethod;

    /**
     * API名称.
     */
    @Schema(description = "API名称")
    private String apiName;

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

    /**
     * 是否隐藏.
     */
    @Schema(description = "是否隐藏")
    private Boolean hideFlag;
}
