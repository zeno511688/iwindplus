/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.system;

import com.iwindplus.mgt.domain.dto.system.ServerRouteParamDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 服务路由视图对象.
 *
 * @author zengdegui
 * @since 2020/4/14
 */
@Schema(description = "服务路由视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ServerRouteDefinitionVO implements Serializable {

    /**
     * 路由ID.
     */
    @Schema(description = "路由ID")
    private String id;

    /**
     * 服务地址.
     */
    @Schema(description = "服务地址")
    private String uri;

    /**
     * 路由规则.
     */
    @Schema(description = "路由规则")
    private List<ServerRouteParamDTO> predicates;

    /**
     * 路由过滤规则.
     */
    @Schema(description = "路由过滤规则")
    private List<ServerRouteParamDTO> filters;

    /**
     * 元数据.
     */
    @Schema(description = "元数据")
    private Map<String, Object> metadata;

    /**
     * 排序号.
     */
    @Schema(description = "排序号")
    private Integer order;
}
