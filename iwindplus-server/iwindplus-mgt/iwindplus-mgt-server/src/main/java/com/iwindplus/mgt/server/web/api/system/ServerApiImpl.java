/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.web.api.system;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.mgt.api.system.ServerApi;
import com.iwindplus.mgt.domain.vo.system.ServerRouteDefinitionVO;
import com.iwindplus.mgt.server.service.system.ServerService;
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

    private final ServerService serverService;

    @Override
    public ResultVO<List<ServerRouteDefinitionVO>> listRouteDefinition() {
        final List<ServerRouteDefinitionVO> data = this.serverService.listRouteDefinition();
        return ResultVO.success(data);
    }
}
