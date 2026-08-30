/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.base.async.task.web;

import com.iwindplus.base.async.task.domain.dto.AsyncTaskGroupSearchDTO;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskGroupVO;
import com.iwindplus.base.async.task.service.AsyncTaskService;
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
 * 异步任务相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "异步任务接口")
@Slf4j
@RestController
@ConditionalOnProperty(
    prefix = "async-task.web",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@RequestMapping("${async-task.web.path:admin/asyncTask}")
@Validated
@RequiredArgsConstructor
public class AsyncTaskController extends BaseController {

    private final AsyncTaskService asyncTaskService;

    /**
     * 详情.
     *
     * @param entity 对象
     * @return ResultVO<AsyncTaskGroupVO>
     */
    @Operation(summary = "详情")
    @GetMapping("getGroupDetail")
    public ResultVO<AsyncTaskGroupVO> getGroupDetail(@Validated AsyncTaskGroupSearchDTO entity) {
        AsyncTaskGroupVO data = this.asyncTaskService.getGroupDetail(entity);
        return ResultVO.success(data);
    }
}
