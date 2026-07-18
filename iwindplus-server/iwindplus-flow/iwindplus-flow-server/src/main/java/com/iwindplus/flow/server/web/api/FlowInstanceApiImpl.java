/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.web.api;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.flow.api.FlowInstanceApi;
import com.iwindplus.flow.domain.dto.FlowStartInstanceDTO;
import com.iwindplus.flow.domain.vo.FlowStartInstanceVO;
import com.iwindplus.flow.server.core.FlowEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流程实例相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2026/05/21 23:27
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class FlowInstanceApiImpl implements FlowInstanceApi {

    private final FlowEngine flowEngine;

    @Override
    public ResultVO<FlowStartInstanceVO> startInstance(FlowStartInstanceDTO entity) {
        return ResultVO.success(flowEngine.instanceAction().startInstance(entity));
    }
}
