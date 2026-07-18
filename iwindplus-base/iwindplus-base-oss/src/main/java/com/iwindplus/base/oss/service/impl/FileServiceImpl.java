/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.PathUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.support.SupplierThrowable;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.oss.service.FileService;
import com.iwindplus.base.util.FilesUtil;
import com.iwindplus.base.util.HttpsUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件操作业务层接口实现类.
 *
 * @author zengdegui
 * @since 2019/8/14
 */
@Slf4j
public class FileServiceImpl extends AbstractBaseServiceImpl implements FileService {

    @Autowired
    private ResourceLoader resourceLoader;

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
    public UploadVO uploadFile(byte[] data, String prefix, String sourceFileName, Boolean renamed, Boolean returnAbsolutePath) {
        this.checkFile(data);
        String fileName = this.getNewFileName(renamed, sourceFileName);
        String relativePath = this.getRelativePath(prefix, fileName);
        return this.getUploadVO(data, relativePath, sourceFileName, returnAbsolutePath);
    }

    @Override
    public UploadVO uploadFile(byte[] data, String relativePath, String sourceFileName, Boolean returnAbsolutePath) {
        this.checkFile(data);
        return this.getUploadVO(data, relativePath, sourceFileName, returnAbsolutePath);
    }

    @Override
    public UploadVO uploadFile(MultipartFile file, String prefix, Boolean renamed, Boolean returnAbsolutePath) {
        this.checkFile(file);
        String sourceFileName = file.getOriginalFilename();
        String fileName = this.getNewFileName(renamed, sourceFileName);
        String relativePath = this.getRelativePath(prefix, fileName);
        return this.getUploadVO(file::getInputStream, relativePath, sourceFileName, returnAbsolutePath);
    }

    @Override
    public UploadVO uploadFile(MultipartFile file, String relativePath, Boolean returnAbsolutePath) {
        this.checkFile(file);
        String sourceFileName = file.getOriginalFilename();
        return this.getUploadVO(file::getInputStream, relativePath, sourceFileName, returnAbsolutePath);
    }

    @Override
    public UploadVO uploadFile(File file, String prefix, Boolean renamed, Boolean returnAbsolutePath) {
        byte[] data = FilesUtil.getBytes(file);
        this.checkFile(data);
        String sourceFileName = file.getName();
        String fileName = this.getNewFileName(renamed, sourceFileName);
        String relativePath = this.getRelativePath(prefix, fileName);
        return this.getUploadVO(data, relativePath, sourceFileName, returnAbsolutePath);
    }

    @Override
    public UploadVO uploadFile(File file, String relativePath, Boolean returnAbsolutePath) {
        byte[] data = FilesUtil.getBytes(file);
        this.checkFile(data);
        String sourceFileName = file.getName();
        return this.getUploadVO(data, relativePath, sourceFileName, returnAbsolutePath);
    }

    @Override
    public boolean removeFiles(List<String> relativePaths) {
        String rootPath = this.getRootPath();
        List<Path> pathList = relativePaths.stream()
            .map(relativePath -> Paths.get(rootPath, relativePath))
            .filter(path -> Files.exists(path))
            .collect(Collectors.toList());
        try {
            for (Path path : pathList) {
                Files.delete(path);
            }
            return true;
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_DELETE_ERROR);
        }
    }

    @Override
    public FilePathVO getSignUrl(String relativePath, Integer timeout) {
        return null;
    }

    @Override
    public List<FilePathVO> listSignUrl(List<String> relativePaths, Integer timeout, DtpExecutor taskExecutor) {
        return null;
    }

    @Override
    public void downloadFile(HttpServletResponse response, String relativePath, String fileName) {
        String rootPath = this.getRootPath();
        Path absolutePath = Paths.get(rootPath, relativePath);
        if (!absolutePath.startsWith(Paths.get(rootPath).normalize())) {
            throw new BizException(BizCodeEnum.FILE_DOWNLOAD_ERROR);
        }
        try {
            FilesUtil.downloadFile(Files.newInputStream(absolutePath), this.getNewFileName(relativePath, fileName), response);
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_DOWNLOAD_ERROR);
        }
    }

    @Override
    public void downloadResourceFile(HttpServletResponse response, String relativePath, String fileName) {
        Resource resource = this.getResource(relativePath);
        try (InputStream inputStream = resource.getInputStream()) {
            FilesUtil.downloadFile(inputStream, super.getNewFileName(relativePath, fileName), response);
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_DOWNLOAD_ERROR);
        }
    }

    @Override
    public void downloadRemoteFile(HttpServletResponse response, String absolutePath, String fileName) {
        final byte[] bytes = HttpsUtil.downloadBytes(absolutePath);
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            FilesUtil.downloadFile(inputStream, super.getNewFileName(absolutePath, fileName), response);
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_DOWNLOAD_ERROR);
        }
    }

    private Path resolveLocked(String root, String relative) {
        Path rootPath = Paths.get(root).toAbsolutePath().normalize();
        return rootPath.resolve(relative).normalize();
    }

    private UploadVO getUploadVO(byte[] data, String relativePath, String sourceFileName, Boolean returnAbsolutePath) {
        Path absolutePath = resolveLocked(getRootPath(), relativePath);
        PathUtil.mkParentDirs(absolutePath);
        try {
            Files.write(absolutePath, data, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_UPLOAD_ERROR);
        }
        return getUploadVO(relativePath, sourceFileName, returnAbsolutePath, absolutePath);
    }

    private UploadVO getUploadVO(SupplierThrowable<InputStream> supplier, String relativePath,
        String sourceFileName, Boolean returnAbsolutePath) {
        Path absolutePath = resolveLocked(getRootPath(), relativePath);
        PathUtil.mkParentDirs(absolutePath);
        try (InputStream in = supplier.get()) {
            Files.copy(in, absolutePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Throwable ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_UPLOAD_ERROR);
        }
        return getUploadVO(relativePath, sourceFileName, returnAbsolutePath, absolutePath);
    }

    private UploadVO getUploadVO(String relativePath, String sourceFileName, Boolean returnAbsolutePath, Path absolutePath) {
        File file = absolutePath.toFile();
        return UploadVO.builder()
            .sourceFileName(sourceFileName)
            .fileName(FileUtil.getName(relativePath))
            .fileSize(file.length())
            .relativePath(relativePath)
            .accessDomain(Optional.ofNullable(returnAbsolutePath).orElse(Boolean.TRUE)
                ? HttpsUtil.getRequestContextPath(null) : null)
            .absolutePath(Optional.ofNullable(returnAbsolutePath).orElse(Boolean.TRUE)
                ? file.getAbsolutePath() : null)
            .build();
    }
}
