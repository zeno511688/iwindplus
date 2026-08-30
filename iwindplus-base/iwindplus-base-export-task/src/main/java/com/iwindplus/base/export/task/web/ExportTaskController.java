/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.web;

import com.iwindplus.base.export.task.domain.vo.ExportTaskVO;
import com.iwindplus.base.export.task.service.ExportTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 导出任务Controller.
 *
 * @author zengdegui
 * @since 2026/08/27
 */
@Tag(name = "导出任务接口")
@Slf4j
@RestController
@ConditionalOnProperty(
    prefix = "export-task.web",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@RequestMapping("${export-task.web.path:admin/exportTask}")
@Validated
@RequiredArgsConstructor
public class ExportTaskController {

    @Resource
    private ExportTaskService exportTaskService;

    /**
     * 查询导出任务进度.
     *
     * @param id 任务ID
     * @return 导出任务VO
     */
    @GetMapping("{id}")
    @Operation(summary = "查询导出任务进度")
    public ExportTaskVO getProgress(@PathVariable Long id) {
        return this.exportTaskService.getDetail(id);
    }
}
