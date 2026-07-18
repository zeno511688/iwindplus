/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.api;

import com.iwindplus.base.domain.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * tcc全局事务相关接口.
 *
 * @author zengdegui
 * @since 2024/08/24 15:12
 */
public interface TccGlobalTxApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/tccGlobalTx/";

    /**
     * 开启全局事务.
     *
     * @param bizType        业务类型
     * @param timeoutSeconds 超时时间
     * @return ResultVO<String>
     */
    @Operation(summary = "开启全局事务")
    @GetMapping(API_PREFIX + "begin")
    ResultVO<String> begin(@RequestParam(value = "bizType") String bizType,
        @RequestParam(value = "timeoutSeconds") Long timeoutSeconds);

    /**
     * 提交全局事务.
     *
     * @param xid 全局事务ID
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "提交全局事务")
    @GetMapping(API_PREFIX + "confirm")
    ResultVO<Boolean> confirm(@RequestParam(value = "xid") String xid);

    /**
     * 回滚全局事务.
     *
     * @param xid 全局事务ID
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "回滚全局事务")
    @GetMapping(API_PREFIX + "cancel")
    ResultVO<Boolean> cancel(@RequestParam(value = "xid") String xid);
}
