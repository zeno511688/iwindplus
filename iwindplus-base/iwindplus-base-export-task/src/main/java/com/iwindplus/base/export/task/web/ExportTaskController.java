/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.web;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.export.task.domain.enums.ExportTaskStatusEnum;
import com.iwindplus.base.export.task.domain.property.ExportTaskProperty;
import com.iwindplus.base.export.task.domain.vo.ExportTaskVO;
import com.iwindplus.base.export.task.service.ExportTaskService;
import com.iwindplus.base.oss.domain.dto.OssCloudDownloadDTO;
import com.iwindplus.base.oss.factory.OssExecuteHandlerFactory;
import com.iwindplus.base.oss.support.OssExecuteHandler;
import com.iwindplus.base.util.FilesUtil;
import com.iwindplus.base.web.controller.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
@RequestMapping("${export-task.web.path:admin/report/exportTask}")
@Validated
@RequiredArgsConstructor
public class ExportTaskController extends BaseController {

    private final ExportTaskProperty property;
    private final ExportTaskService exportTaskService;
    private final ObjectProvider<OssExecuteHandlerFactory> ossExecuteHandlerFactoryProvider;

    /**
     * 查询导出任务进度.
     *
     * @param id 任务ID
     * @return 导出任务VO
     */
    @GetMapping("getDetail")
    @Operation(summary = "查询导出任务进度")
    public ExportTaskVO getDetail(@RequestParam Long id) {
        return this.exportTaskService.getDetail(id);
    }

    /**
     * 下载导出文件.
     *
     * @param id       任务ID
     * @param response 响应
     */
    @GetMapping("download")
    @Operation(summary = "下载导出文件")
    public void download(@RequestParam Long id, HttpServletResponse response) {
        final ExportTaskVO task = this.getExportTaskVO(id);

        final ExportTaskProperty.OssConfig ossConfig = this.property.getOss();
        if (ossConfig == null || Boolean.FALSE.equals(ossConfig.getEnabled())) {
            this.downloadFile(id, response, task);
            return;
        }

        final OssExecuteHandlerFactory factory = this.ossExecuteHandlerFactoryProvider.getIfAvailable();
        final OssExecuteHandler ossHandler = this.resolveOssHandler(factory, ossConfig.getCode());
        if (ossHandler == null) {
            this.downloadFile(id, response, task);
            return;
        }

        final OssCloudDownloadDTO request = OssCloudDownloadDTO.builder()
            .bucketName(ossConfig.getBucketName())
            .accessDomain(ossConfig.getAccessDomain())
            .response(response)
            .relativePath(task.getFilePath())
            .fileName(task.getFileName())
            .build();
        ossHandler.downloadFile(request);
    }

    private ExportTaskVO getExportTaskVO(Long id) {
        final ExportTaskVO task = this.exportTaskService.getDetail(id);
        if (task == null) {
            throw new BizException(BizCodeEnum.DATA_NOT_EXIST);
        }
        if (ExportTaskStatusEnum.SUCCESS != task.getStatus()) {
            throw new BizException(BizCodeEnum.UNSUPPORTED_OPERATION);
        }
        if (task.getFilePath() == null || task.getFilePath().isBlank()) {
            throw new BizException(BizCodeEnum.FILE_NOT_FOUND);
        }
        return task;
    }

    private void downloadFile(Long id, HttpServletResponse response, ExportTaskVO task) {
        final File file = new File(task.getFilePath());
        if (!file.exists() || !file.isFile()) {
            throw new BizException(BizCodeEnum.FILE_NOT_FOUND);
        }

        try (InputStream inputStream = new FileInputStream(file)) {
            FilesUtil.downloadFile(inputStream, task.getFileName(), response);
        } catch (FileNotFoundException ex) {
            log.error("exportTask download file not found. id={} filePath={}", id, task.getFilePath(), ex);
            throw new BizException(BizCodeEnum.FILE_NOT_FOUND);
        } catch (Exception ex) {
            log.error("exportTask download file failed. id={} filePath={}", id, task.getFilePath(), ex);
            throw new BizException(BizCodeEnum.FILE_DOWNLOAD_ERROR);
        }
    }

    private OssExecuteHandler resolveOssHandler(OssExecuteHandlerFactory factory, String code) {
        if (CharSequenceUtil.isBlank(code)) {
            return factory.getDefaultHandler();
        }
        return factory.getHandler(property.getOss().getType(), code);
    }
}
