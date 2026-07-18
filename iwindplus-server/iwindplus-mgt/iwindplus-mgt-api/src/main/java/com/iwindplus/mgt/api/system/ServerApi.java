/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.api.system;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.domain.vo.system.ServerRouteDefinitionVO;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 服务相关接口.
 *
 * @author zengdegui
 * @since 2020年4月1日
 */
public interface ServerApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/server/";

    /**
     * 所有服务路由.
     *
     * @return ResultVO<List < ServerRouteDefinitionVO>>
     */
    @Operation(summary = "所有服务路由")
    @GetMapping(API_PREFIX + "listRouteDefinition")
    ResultVO<List<ServerRouteDefinitionVO>> listRouteDefinition();
}
