/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.report.interfaces.controller.mgt;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.export.task.domain.dto.ExportTaskSubmitDTO;
import com.iwindplus.base.export.task.domain.vo.ExportTaskSubmitVO;
import com.iwindplus.base.export.task.executor.ExportTaskExecutor;
import com.iwindplus.base.web.controller.BaseController;
import com.iwindplus.mgt.api.system.dto.IpBlackListSearchDTO;
import com.iwindplus.report.application.service.mgt.handler.IpBlackListExportTaskHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * IP黑名单相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2020/9/20
 */
@Tag(name = "IP黑名单接口")
@Slf4j
@RestController
@RequestMapping("admin/report/ipBlackList")
@Validated
@RequiredArgsConstructor
public class IpBlackListController extends BaseController {

    private final ExportTaskExecutor exportTaskExecutor;

    /**
     * 提交导出任务.
     *
     * @param entity 对象
     * @return ResultVO<ExportTaskSubmitVO>
     */
    @Operation(summary = "提交导出任务")
    @PostMapping("exportTask")
    public ResultVO<ExportTaskSubmitVO> exportTask(@RequestBody @Validated IpBlackListSearchDTO entity) {
        final ExportTaskSubmitDTO param = ExportTaskSubmitDTO
            .builder()
            .executorClass(IpBlackListExportTaskHandler.class)
            .build();
        param.setQueryParam(entity);

        final ExportTaskSubmitVO data = this.exportTaskExecutor.submit(param);
        return ResultVO.success(data);
    }
}
