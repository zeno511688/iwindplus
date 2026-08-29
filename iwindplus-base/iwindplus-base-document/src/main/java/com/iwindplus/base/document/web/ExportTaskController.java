/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.document.web;

import com.iwindplus.base.document.domain.vo.ExportTaskVO;
import com.iwindplus.base.document.service.ExportTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
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
    prefix = "document.export-task.web",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@RequestMapping("${document.export-task.web.path:admin/exportTask}")
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

    /**
     * 下载导出文件.
     *
     * @param id       任务ID
     * @param response 响应
     */
    @GetMapping("download/{id}")
    @Operation(summary = "下载导出文件")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        ExportTaskVO task = this.exportTaskService.getDetail(id);
        if (Objects.isNull(task)) {
            log.warn("导出任务不存在，id={}", id);
            return;
        }

        if (Objects.isNull(task.getFilePath())) {
            log.warn("导出文件路径为空，id={}", id);
            return;
        }

        File file = new File(task.getFilePath());
        if (!file.exists()) {
            log.warn("导出文件不存在，filePath={}", task.getFilePath());
            return;
        }

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(task.getFileName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + encodedFileName);

            try (FileInputStream fis = new FileInputStream(file);
                OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
        } catch (Exception e) {
            log.error("下载导出文件失败，id={}", id, e);
        }
    }
}
