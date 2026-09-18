/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.application.service;

import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.PreUploadVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.oss.domain.dto.OssCloudDownloadDTO;
import com.iwindplus.base.oss.domain.dto.OssCloudListSignUrlDTO;
import com.iwindplus.base.oss.domain.dto.OssCloudPreUploadDTO;
import com.iwindplus.base.oss.domain.dto.OssCloudRemoveDTO;
import com.iwindplus.base.oss.domain.dto.OssCloudUploadDTO;
import com.iwindplus.base.oss.factory.OssExecuteHandlerFactory;
import com.iwindplus.base.oss.support.OssExecuteHandler;
import com.iwindplus.integr.application.query.OssTplQueryService;
import com.iwindplus.integr.common.constant.IntegrConstant;
import com.iwindplus.integr.api.dto.OssPreUploadDTO;
import com.iwindplus.integr.api.dto.OssUploadFileExtendDTO;
import com.iwindplus.integr.application.query.vo.OssTplVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 对象存储业务层接口类.
 *
 * @author zengdegui
 * @since 2021/7/20
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class OssApplicationService {

    @Resource
    private OssExecuteHandlerFactory ossExecuteHandlerFactory;

    @Resource
    private OssTplQueryService ossTplQueryService;

    @Resource(name = IntegrConstant.THREAD_POOL_BEAN_NAME_OSS)
    private DtpExecutor threadPoolExecutor;

    /**
     * 文件预上传（预签名直传）.
     *
     * @param entity 对象
     * @return PreUploadVO
     */
    public PreUploadVO preUpload(OssPreUploadDTO entity) {
        final OssTplVO ossTpl = this.ossTplQueryService.getByCode(entity.getTplCode());
        final OssExecuteHandler handler = this.ossExecuteHandlerFactory.getHandler(ossTpl.getType(), entity.getCode());
        final OssCloudPreUploadDTO request = OssCloudPreUploadDTO.builder()
            .bucketName(ossTpl.getBucketName())
            .accessDomain(ossTpl.getAccessDomain())
            .relativePath(entity.getRelativePath())
            .contentType(entity.getContentType())
            .timeout(entity.getTimeout())
            .renamed(entity.getRenamed())
            .build();
        return handler.preUpload(request);
    }

    /**
     * 文件上传.
     *
     * @param entity 对象
     * @return UploadVO
     */
    public UploadVO uploadFile(OssUploadFileExtendDTO entity) {
        final OssTplVO ossTpl = this.ossTplQueryService.getByCode(entity.getTplCode());
        final OssExecuteHandler handler = this.ossExecuteHandlerFactory.getHandler(ossTpl.getType(), entity.getCode());
        final OssCloudUploadDTO request = OssCloudUploadDTO.builder()
            .bucketName(ossTpl.getBucketName())
            .accessDomain(ossTpl.getAccessDomain())
            .data(entity.getData())
            .file(entity.getFile())
            .url(entity.getUrl())
            .relativePath(entity.getRelativePath())
            .sourceFileName(entity.getSourceFileName())
            .contentType(entity.getContentType())
            .renamed(entity.getRenamed())
            .build();
        return handler.uploadFile(request);
    }

    /**
     * 文件下载.
     *
     * @param code         配置编码（必填）
     * @param tplCode      模板编码（必填）
     * @param response     响应（必填）
     * @param relativePath 相对路径（必填）
     * @param fileName     新文件名，不包含文件后缀（可选）
     */
    public void downloadFile(String code, String tplCode, HttpServletResponse response, String relativePath, String fileName) {
        final OssTplVO ossTpl = this.ossTplQueryService.getByCode(tplCode);
        final OssExecuteHandler handler = this.ossExecuteHandlerFactory.getHandler(ossTpl.getType(), code);
        final OssCloudDownloadDTO request = OssCloudDownloadDTO.builder()
            .bucketName(ossTpl.getBucketName())
            .accessDomain(ossTpl.getAccessDomain())
            .response(response)
            .relativePath(relativePath)
            .fileName(fileName)
            .build();
        this.threadPoolExecutor.execute(() -> handler.downloadFile(request));
    }

    /**
     * 批量获取访问路径.
     *
     * @param code          配置编码（必填）
     * @param tplCode       模板编码（必填）
     * @param relativePaths 相对路径集合（必填）
     * @param timeout       过期时间（单位：分钟，默认：1）
     * @return List<FilePathVO>
     */
    public List<FilePathVO> listSignUrl(String code, String tplCode, List<String> relativePaths, Integer timeout) {
        final OssTplVO ossTpl = this.ossTplQueryService.getByCode(tplCode);
        final OssExecuteHandler handler = this.ossExecuteHandlerFactory.getHandler(ossTpl.getType(), code);
        final OssCloudListSignUrlDTO request = OssCloudListSignUrlDTO.builder()
            .bucketName(ossTpl.getBucketName())
            .accessDomain(ossTpl.getAccessDomain())
            .relativePaths(relativePaths)
            .timeout(timeout)
            .threadPoolExecutor(threadPoolExecutor)
            .build();
        return handler.listSignUrl(request);
    }

    /**
     * 批量删除文件.
     *
     * @param code          配置编码（必填）
     * @param tplCode       模板编码（必填）
     * @param relativePaths 相对路径集合（必填）
     */
    public void removeFiles(String code, String tplCode, List<String> relativePaths) {
        final OssTplVO ossTpl = this.ossTplQueryService.getByCode(tplCode);
        final OssExecuteHandler handler = this.ossExecuteHandlerFactory.getHandler(ossTpl.getType(), code);
        final OssCloudRemoveDTO request = OssCloudRemoveDTO.builder()
            .bucketName(ossTpl.getBucketName())
            .relativePaths(relativePaths)
            .build();
        this.threadPoolExecutor.execute(() -> handler.removeFiles(request));
    }

}
