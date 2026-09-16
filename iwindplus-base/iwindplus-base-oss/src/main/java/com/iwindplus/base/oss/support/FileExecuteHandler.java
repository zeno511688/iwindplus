/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.support;

import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.oss.domain.dto.OssDownloadDTO;
import com.iwindplus.base.oss.domain.dto.OssDownloadRemoteDTO;
import com.iwindplus.base.oss.domain.dto.OssRemoveDTO;
import com.iwindplus.base.oss.domain.dto.OssUploadDTO;
import org.springframework.core.io.Resource;

/**
 * 文件操作执行器接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface FileExecuteHandler {

    /**
     * 文件上传.
     *
     * @param entity 上传请求参数（必填）
     * @return UploadVO
     */
    UploadVO uploadFile(OssUploadDTO entity);

    /**
     * 批量删除上传的文件.
     *
     * @param entity 删除请求参数（必填）
     * @return boolean
     */
    boolean removeFiles(OssRemoveDTO entity);

    /**
     * 获取（src/main/resources下文件）
     *
     * @param relativePath 相对路径（必填）
     * @return Resource
     */
    Resource getResource(String relativePath);

    /**
     * 文件下载.
     *
     * @param entity 下载请求参数（必填）
     */
    void downloadFile(OssDownloadDTO entity);

    /**
     * 文件下载（src/main/resources下文件）.
     *
     * @param entity 下载请求参数（必填）
     */
    void downloadResourceFile(OssDownloadDTO entity);

    /**
     * 远程文件下载.
     *
     * @param entity 远程下载请求参数（必填）
     */
    void downloadRemoteFile(OssDownloadRemoteDTO entity);
}
