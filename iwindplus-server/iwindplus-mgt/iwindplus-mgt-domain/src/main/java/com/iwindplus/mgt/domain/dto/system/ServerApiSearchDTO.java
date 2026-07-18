/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.system;


import com.iwindplus.base.domain.dto.DbPageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 服务API搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "服务API搜索数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ServerApiSearchDTO extends DbPageDTO {

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
     * API名称.
     */
    @Schema(description = "API名称")
    private String apiName;

    /**
     * API路径.
     */
    @Schema(description = "API路径")
    private String apiUrl;
}
