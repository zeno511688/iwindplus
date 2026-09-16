/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.infrastructure.client.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 路由参数数据传输对象.
 *
 * @author zengdegui
 * @since 2024/12/30 23:30
 */
@Schema(description = "路由参数数据传输对象")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerRouteParamVO implements Serializable {

    /**
     * 名称.
     */
    @Schema(description = "名称")
    private String name;

    /**
     * args.
     */
    @Schema(description = "args")
    private Map<String, String> args;
}
