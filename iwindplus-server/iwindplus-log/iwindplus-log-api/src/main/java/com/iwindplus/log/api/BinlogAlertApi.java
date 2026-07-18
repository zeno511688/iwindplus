/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.api;

import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.log.domain.dto.BinlogAlertDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * binlog告警相关接口.
 *
 * @author zengdegui
 * @since 2024/4/10
 */
public interface BinlogAlertApi {

    /**
     * API前缀.
     */
    String API_PREFIX = "inner/binlog/alert/";

    /**
     * 添加binlog告警.
     *
     * @param entity 对象
     * @return ResultVO<Boolean>
     */
    @Operation(summary = "添加binlog告警")
    @PostMapping(API_PREFIX + "save")
    ResultVO<Boolean> save(@RequestBody @Validated({SaveGroup.class}) BinlogAlertDTO entity);
}
