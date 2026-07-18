/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.service;

import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.UploadVO;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 对象存储业务层基础接口类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface OssBaseService {

    /**
     * 文件上传.
     *
     * @param data               字节数组（必填）
     * @param prefix             存储目录前缀（必填）
     * @param sourceFileName     源文件名（必填）
     * @param renamed            是否重命名文件名（可选，默认：true）
     * @param returnAbsolutePath 是否返回绝对路径（可选，默认：true）
     * @return UploadVO
     */
    UploadVO uploadFile(byte[] data, String prefix, String sourceFileName, Boolean renamed, Boolean returnAbsolutePath);

    /**
     * 文件上传.
     *
     * @param data               字节数组（必填）
     * @param relativePath       相对路径（必填）
     * @param sourceFileName     源文件名（必填）
     * @param returnAbsolutePath 是否返回绝对路径（可选，默认：true）
     * @return UploadVO
     */
    UploadVO uploadFile(byte[] data, String relativePath, String sourceFileName, Boolean returnAbsolutePath);

    /**
     * 文件上传.
     *
     * @param file               文件（必填）
     * @param prefix             存储目录前缀（必填）
     * @param renamed            是否重命名文件名（可选，默认：true）
     * @param returnAbsolutePath 是否返回绝对路径（可选，默认：true）
     * @return UploadVO
     */
    UploadVO uploadFile(MultipartFile file, String prefix, Boolean renamed, Boolean returnAbsolutePath);

    /**
     * 文件上传.
     *
     * @param file               文件（必填）
     * @param relativePath       相对路径（必填）
     * @param returnAbsolutePath 是否返回绝对路径（可选，默认：true）
     * @return UploadVO
     */
    UploadVO uploadFile(MultipartFile file, String relativePath, Boolean returnAbsolutePath);

    /**
     * 文件上传.
     *
     * @param file               文件（必填）
     * @param prefix             存储目录前缀（必填）
     * @param renamed            是否重命名文件名（可选，默认：true）
     * @param returnAbsolutePath 是否返回绝对路径（可选，默认：true）
     * @return UploadVO
     */
    UploadVO uploadFile(File file, String prefix, Boolean renamed, Boolean returnAbsolutePath);

    /**
     * 文件上传.
     *
     * @param file               文件（必填）
     * @param relativePath       相对路径（必填）
     * @param returnAbsolutePath 是否返回绝对路径（可选，默认：true）
     * @return UploadVO
     */
    UploadVO uploadFile(File file, String relativePath, Boolean returnAbsolutePath);

    /**
     * 获取文件访问路径.
     *
     * @param relativePath 相对路径（必填）
     * @param timeout      过期时间（可选，单位：分钟，默认：60）
     * @return FilePathVO
     */
    FilePathVO getSignUrl(String relativePath, Integer timeout);

    /**
     * 获取文件访问路径.
     *
     * @param relativePaths 相对路径集合（必填）
     * @param timeout       过期时间（可选，单位：分钟，默认：60）
     * @param taskExecutor  线程池
     * @return List<FilePathVO>
     */
    List<FilePathVO> listSignUrl(List<String> relativePaths, Integer timeout, DtpExecutor taskExecutor);

    /**
     * 批量删除上传的文件.
     *
     * @param relativePaths 相对路径集合（必填）
     * @return boolean
     */
    boolean removeFiles(List<String> relativePaths);

    /**
     * 文件下载.
     *
     * @param response     响应（必填）
     * @param relativePath 相对路径（必填）
     * @param fileName     新文件名（可选）
     */
    void downloadFile(HttpServletResponse response, String relativePath, String fileName);

}
