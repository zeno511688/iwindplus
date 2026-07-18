/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.server.service;

import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.setup.domain.dto.OssUploadByteDTO;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 对象存储业务层接口类.
 *
 * @author zengdegui
 * @since 2021/7/20
 */
public interface OssService {

    /**
     * 文件上传.
     *
     * @param entity 对象
     * @return UploadVO
     */
    UploadVO uploadByte(OssUploadByteDTO entity);

    /**
     * 文件下载.
     *
     * @param tplCode      模板编码（必填）
     * @param response     响应（必填）
     * @param relativePath 相对路径（必填）
     * @param fileName     新文件名，不包含文件后缀（可选）
     */
    void downloadFile(String tplCode, HttpServletResponse response, String relativePath, String fileName);

    /**
     * 批量获取访问路径.
     *
     * @param tplCode       模板编码（必填）
     * @param relativePaths 相对路径集合（必填）
     * @param timeout       过期时间（单位：分钟，默认：60）
     * @return List<FilePathVO>
     */
    List<FilePathVO> listSignUrl(String tplCode, List<String> relativePaths, Integer timeout);

    /**
     * 批量删除文件.
     *
     * @param tplCode       模板编码（必填）
     * @param relativePaths 相对路径集合（必填）
     */
    void removeFiles(String tplCode, List<String> relativePaths);
}
