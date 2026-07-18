/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.web.api;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.dtx.api.TccBranchTxApi;
import com.iwindplus.dtx.domain.dto.TccBranchTxDTO;
import com.iwindplus.dtx.server.coordinator.TccCoordinator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * tcc分支事务相关内部接口实现类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@RestController
@RequestMapping
@Validated
@Slf4j
@RequiredArgsConstructor
public class TccBranchTxApiImpl implements TccBranchTxApi {

    private final TccCoordinator tccCoordinator;

    @Override
    public ResultVO<Long> register(TccBranchTxDTO entity) {
        Long data = this.tccCoordinator.register(entity);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<Boolean> trySuccess(Long id) {
        final boolean data = this.tccCoordinator.trySuccess(id);
        return ResultVO.success(data);
    }

    @Override
    public ResultVO<Boolean> tryFail(Long id) {
        final boolean data = this.tccCoordinator.tryFail(id);
        return ResultVO.success(data);
    }
}
