/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;

/**
 * 文件操作业务层接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface FileService extends OssBaseService {

    /**
     * 获取（src/main/resources下文件）
     *
     * @param relativePath 相对路径（必填）
     * @return Resource
     */
    Resource getResource(String relativePath);

    /**
     * 文件下载（src/main/resources下文件）.
     *
     * @param response     响应（必填）
     * @param relativePath 相对路径（必填）
     * @param fileName     新文件名（可选）
     */
    void downloadResourceFile(HttpServletResponse response, String relativePath, String fileName);

    /**
     * 远程文件下载.
     *
     * @param response     响应（必填）
     * @param absolutePath 绝对路径（必填）
     * @param fileName     新文件名（可选）
     */
    void downloadRemoteFile(HttpServletResponse response, String absolutePath, String fileName);
}
