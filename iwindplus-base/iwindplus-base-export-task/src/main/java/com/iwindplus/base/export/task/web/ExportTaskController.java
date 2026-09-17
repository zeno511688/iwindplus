/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.web;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.export.task.domain.enums.ExportTaskStatusEnum;
import com.iwindplus.base.export.task.domain.property.ExportTaskProperty;
import com.iwindplus.base.export.task.domain.vo.ExportTaskVO;
import com.iwindplus.base.export.task.service.ExportTaskService;
import com.iwindplus.base.http.client.factory.HttpClientExecuteHandlerFactory;
import com.iwindplus.base.util.FilesUtil;
import com.iwindplus.base.util.HttpsUtil;
import com.iwindplus.base.web.controller.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory;

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
            this.downloadLocalFile(response, task);
            return;
        }

        final String filePath = task.getFilePath();
        if (CharSequenceUtil.isBlank(filePath)) {
            throw new BizException(BizCodeEnum.FILE_NOT_FOUND);
        }

        final Map<String, Object> query = Map.of(
            "code", ossConfig.getCode(),
            "tplCode", ossConfig.getTplCode(),
            "relativePaths", List.of(filePath),
            "timeout", ossConfig.getSignTimeout()
        );

        final ResultVO<List<FilePathVO>> responseResult = httpClientExecuteHandlerFactory
            .getDefaultHandler()
            .get(
                ossConfig.getListSignUrl(),
                query,
                null,
                new TypeReference<>() {
                }
            );
        responseResult.errorThrow();
        final List<FilePathVO> dataList = responseResult.getBizData();
        if (CollUtil.isEmpty(dataList)) {
            throw new BizException(BizCodeEnum.FILE_NOT_FOUND);
        }
        final FilePathVO filePathVO = dataList.get(0);
        this.downloadRemoteFile(response, task, filePathVO);
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

    private void downloadLocalFile(HttpServletResponse response, ExportTaskVO task) {
        final File file = new File(task.getFilePath());
        if (!file.exists() || !file.isFile()) {
            throw new BizException(BizCodeEnum.FILE_NOT_FOUND);
        }

        try (InputStream inputStream = new FileInputStream(file)) {
            FilesUtil.downloadFile(inputStream, task.getFileName(), response);
        } catch (FileNotFoundException ex) {
            log.error("exportTask download file not found. filePath={}", task.getFilePath(), ex);
            throw new BizException(BizCodeEnum.FILE_NOT_FOUND);
        } catch (Exception ex) {
            log.error("exportTask download file failed. filePath={}", task.getFilePath(), ex);
            throw new BizException(BizCodeEnum.FILE_DOWNLOAD_ERROR);
        }
    }

    public void downloadRemoteFile(HttpServletResponse response, ExportTaskVO task, FilePathVO filePath) {
        final byte[] bytes = HttpsUtil.downloadBytes(filePath.getAbsolutePath());
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            FilesUtil.downloadFile(inputStream, task.getFileName(), response);
        } catch (IOException ex) {
            log.error("exportTask download file failed. filePath={}", filePath.getAbsolutePath(), ex);

            throw new BizException(BizCodeEnum.FILE_DOWNLOAD_ERROR);
        }
    }
}
