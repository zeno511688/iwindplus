/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.web.api;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.dtx.api.TccGlobalTxApi;
import com.iwindplus.dtx.server.coordinator.TccCoordinator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * tcc全局事务相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class TccGlobalTxApiImpl implements TccGlobalTxApi {

    private final TccCoordinator tccCoordinator;

    @Override
    public ResultVO<String> begin(String bizType, Long timeoutSeconds) {
        final String data = this.tccCoordinator.begin(bizType, timeoutSeconds);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<Boolean> confirm(String xid) {
        final boolean data = this.tccCoordinator.confirm(xid);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<Boolean> cancel(String xid) {
        final boolean data = this.tccCoordinator.cancel(xid);
        return ResultVO.success(data);
    }
}
