/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.server.web.api;

import com.iwindplus.base.domain.dto.ValidListDTO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.log.api.GatewayLogApi;
import com.iwindplus.log.domain.dto.GatewayLogDTO;
import com.iwindplus.log.server.service.GatewayLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 网关日志相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class GatewayLogApiImpl extends BaseController implements GatewayLogApi {

    private final GatewayLogService gatewayLogService;

    @Override
    public ResultVO<Boolean> saveBatch(ValidListDTO<GatewayLogDTO> entity) {
        boolean data = this.gatewayLogService.saveBatch(entity.getEntities());
        return ResultVO.success(data);
    }
}
