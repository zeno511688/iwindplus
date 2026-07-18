/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.api;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.dtx.domain.dto.TccBranchTxDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * tcc分支事务相关接口.
 *
 * @author zengdegui
 * @since 2024/08/24 15:12
 */
public interface TccBranchTxApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/tccBranchTx/";

    /**
     * 分支事务注册.
     *
     * @param entity 对象
     * @return ResultVO<Long>
     */
    @Operation(summary = "分支事务注册")
    @PostMapping(API_PREFIX + "register")
    ResultVO<Long> register(@RequestBody @Validated TccBranchTxDTO entity);

    /**
     * Try成功.
     *
     * @param id 主键
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "Try成功")
    @GetMapping(API_PREFIX + "trySuccess")
    ResultVO<Boolean> trySuccess(@RequestParam(value = "id") Long id);

    /**
     * Try失败.
     *
     * @param id 主键
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "Try失败")
    @GetMapping(API_PREFIX + "tryFail")
    ResultVO<Boolean> tryFail(@RequestParam(value = "id") Long id);
}
