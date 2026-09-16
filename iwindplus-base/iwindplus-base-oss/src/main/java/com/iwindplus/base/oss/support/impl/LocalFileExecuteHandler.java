/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.support.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.PathUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.oss.domain.dto.OssDownloadDTO;
import com.iwindplus.base.oss.domain.dto.OssDownloadRemoteDTO;
import com.iwindplus.base.oss.domain.dto.OssRemoveDTO;
import com.iwindplus.base.oss.domain.dto.OssUploadDTO;
import com.iwindplus.base.oss.service.impl.AbstractBaseServiceImpl;
import com.iwindplus.base.util.FilesUtil;
import com.iwindplus.base.util.HttpsUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

/**
 * 本地文件操作执行器接口实现类.
 *
 * @author zengdegui
 * @since 2019/8/14
 */
@Slf4j
public class LocalFileExecuteHandler extends AbstractBaseServiceImpl<Void> implements com.iwindplus.base.oss.support.FileExecuteHandler {

    private final ResourceLoader resourceLoader;

    /**
     * 构造函数.
     *
     * @param multipartProperties 文件上传配置
     * @param resourceLoader      资源加载器
     */
    public LocalFileExecuteHandler(
        MultipartProperties multipartProperties,
        ResourceLoader resourceLoader) {
        super(multipartProperties);
        this.resourceLoader = resourceLoader;
    }

    @Override
    public Resource getResource(String relativePath) {
        StringBuilder sb = new StringBuilder(CommonConstant.FileConstant.CLASSPATH)
            .append(CommonConstant.SymbolConstant.COLON).append(relativePath);
        Resource resource = this.resourceLoader.getResource(sb.toString());
        if (!resource.exists()) {
            throw new BizException(BizCodeEnum.FILE_NOT_FOUND);
        }
        return resource;
    }

    @Override
    public UploadVO uploadFile(OssUploadDTO entity) {
        final MultipartFile file = entity.getFile();
        final byte[] data = entity.getData();

        Assert.isTrue(
            Objects.nonNull(file) || ArrayUtil.isNotEmpty(data),
            "文件内容不能为空"
        );

        if (ArrayUtil.isEmpty(data) && Objects.nonNull(file)) {
            entity.setData(FilesUtil.getBytes(file));
            entity.setContentType(file.getContentType());

            if (CharSequenceUtil.isBlank(entity.getSourceFileName())) {
                entity.setSourceFileName(file.getOriginalFilename());
            }
        }

        super.checkFile(entity.getData());
        return this.getUploadVO(entity);
    }

    @Override
    public boolean removeFiles(OssRemoveDTO entity) {
        String rootPath = this.getRootPath();
        Path rootDir = Paths.get(rootPath).normalize().toAbsolutePath();

        try {
            for (String relativePath : entity.getRelativePaths()) {
                // 规范化路径并检查是否在根目录下
                Path targetPath = Paths.get(rootPath, relativePath).normalize().toAbsolutePath();

                // 检查路径是否在根目录下，防止路径穿越
                if (!targetPath.startsWith(rootDir)) {
                    log.warn("Path traversal attempt detected: {}", relativePath);
                    continue;
                }

                // 检查文件是否存在
                if (!Files.exists(targetPath)) {
                    continue;
                }

                // 检查是否为符号链接，防止符号链接绕过
                if (Files.isSymbolicLink(targetPath)) {
                    log.warn("Symbolic link detected, skipping: {}", relativePath);
                    continue;
                }

                Files.delete(targetPath);
            }
            return true;
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);
            throw new BizException(BizCodeEnum.FILE_DELETE_ERROR);
        }
    }

    @Override
    public void downloadFile(OssDownloadDTO entity) {
        String rootPath = this.getRootPath();
        Path absolutePath = Paths.get(rootPath, entity.getRelativePath());
        if (!absolutePath.startsWith(Paths.get(rootPath).normalize())) {
            throw new BizException(BizCodeEnum.FILE_DOWNLOAD_ERROR);
        }
        try {
            FilesUtil.downloadFile(Files.newInputStream(absolutePath), this.getNewFileName(entity.getRelativePath(), entity.getFileName()),
                entity.getResponse());
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_DOWNLOAD_ERROR);
        }
    }

    @Override
    public void downloadResourceFile(OssDownloadDTO entity) {
        Resource resource = this.getResource(entity.getRelativePath());
        try (InputStream inputStream = resource.getInputStream()) {
            FilesUtil.downloadFile(inputStream, super.getNewFileName(entity.getRelativePath(), entity.getFileName()), entity.getResponse());
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_DOWNLOAD_ERROR);
        }
    }

    @Override
    public void downloadRemoteFile(OssDownloadRemoteDTO entity) {
        final byte[] bytes = HttpsUtil.downloadBytes(entity.getAbsolutePath());
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            FilesUtil.downloadFile(inputStream, super.getNewFileName(entity.getAbsolutePath(), entity.getFileName()), entity.getResponse());
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_DOWNLOAD_ERROR);
        }
    }

    private Path resolveLocked(String root, String relativePath) {
        Path rootPath = Paths.get(root).toAbsolutePath().normalize();
        return rootPath.resolve(relativePath).normalize();
    }

    private UploadVO getUploadVO(OssUploadDTO entity) {
        entity.setRelativePath(this.getRelativePath(entity.getRelativePath(), entity.getRenamed()));

        Path absolutePath = resolveLocked(getRootPath(), entity.getRelativePath());
        PathUtil.mkParentDirs(absolutePath);
        try {
            Files.write(absolutePath, entity.getData(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);

            return UploadVO.builder()
                .sourceFileName(entity.getSourceFileName())
                .fileName(FileUtil.getName(entity.getRelativePath()))
                .fileSize(Files.size(absolutePath))
                .relativePath(entity.getRelativePath())
                .accessDomain(HttpsUtil.getRequestContextPath(null))
                .absolutePath(absolutePath.toString())
                .build();
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_UPLOAD_ERROR);
        }
    }
}
