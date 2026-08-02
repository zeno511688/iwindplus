/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.base.async.cmd.web;

import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdGroupSearchDTO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdGroupVO;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.controller.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 异步命令相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "异步命令接口")
@Slf4j
@RestController
@ConditionalOnProperty(
    prefix = "async-cmd.web",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@RequestMapping("${async-cmd.web.path:admin/asyncCmd}")
@Validated
@RequiredArgsConstructor
public class AsyncCmdController extends BaseController {

    private final AsyncCmdService asyncCmdService;

    /**
     * 详情.
     *
     * @param entity 对象
     * @return ResultVO<AsyncCmdGroupVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getGroupDetail")
    public ResultVO<AsyncCmdGroupVO> getGroupDetail(@Validated AsyncCmdGroupSearchDTO entity) {
        AsyncCmdGroupVO data = this.asyncCmdService.getGroupDetail(entity);
        return ResultVO.success(data);
    }
}
