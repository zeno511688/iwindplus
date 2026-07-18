/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.web.admin;

import com.iwindplus.base.oss.service.FileService;
import com.iwindplus.base.web.controller.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件相关操作接口定义类.
 *
 * @author zengdegui
 * @since 2021/7/20
 */
@Tag(name = "文件接口")
@Slf4j
@RestController
@RequestMapping("admin/mgt/file")
@Validated
@RequiredArgsConstructor
public class FileController extends BaseController {

    private final FileService fileService;

    /**
     * 文件下载-resources目录下.
     *
     * @param relativePath 相对路径（必填）
     * @param fileName     新文件名（可选）
     */
    @Operation(summary = "文件下载-resources目录下")
    @GetMapping("downloadResourceFile")
    public void downloadFile(
        @RequestParam String relativePath,
        @RequestParam(required = false) String fileName) {
        this.fileService.downloadResourceFile(this.getResponse(), relativePath, fileName);
    }
}
