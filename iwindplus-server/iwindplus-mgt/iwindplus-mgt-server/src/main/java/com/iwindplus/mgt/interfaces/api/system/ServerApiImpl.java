/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.interfaces.api.system;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.api.system.ServerApi;
import com.iwindplus.mgt.api.system.vo.ServerRouteDefinitionVO;
import com.iwindplus.mgt.application.query.system.server.ServerQueryService;
import com.iwindplus.mgt.application.service.system.server.ServerApplicationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class ServerApiImpl implements ServerApi {

    private final ServerApplicationService serverApplicationService;
    private final ServerQueryService serverQueryService;

    @Override
    public ResultVO<List<ServerRouteDefinitionVO>> listRouteDefinition() {
        final List<ServerRouteDefinitionVO> data = this.serverQueryService.listRouteDefinition();
        return ResultVO.success(data);
    }
}
