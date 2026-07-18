/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.api;

import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.log.domain.dto.OperationLogDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 操作日志相关接口.
 *
 * @author zengdegui
 * @since 2024/4/10
 */
public interface OperationLogApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/operation/log/";

    /**
     * 添加操作日志.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "添加操作日志")
    @PostMapping(API_PREFIX + "save")
    ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) OperationLogDTO entity);
}
