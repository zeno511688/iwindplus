/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.PathUtil;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.FileConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.oss.domain.constant.OssConstant;
import com.iwindplus.base.oss.service.OssQiniuService;
import com.iwindplus.base.util.FilesUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.BucketInfo;
import com.qiniu.storage.persistent.FileRecorder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 七牛云对象存储操作业务层接口实现类.
 *
 * @author zengdegui
 * @since 2019/8/9
 */
@Slf4j
public class OssQiniuServiceImpl extends AbstractOssBaseServiceImpl implements OssQiniuService {

    @Override
    public UploadVO uploadFile(byte[] data, String prefix, String sourceFileName, Boolean renamed, Boolean returnAbsolutePath) {
        super.checkFile(data);
        String fileName = super.getNewFileName(renamed, sourceFileName);
        String relativePath = super.getRelativePath(prefix, fileName);
        return this.getUploadVO(data, relativePath, sourceFileName, returnAbsolutePath);
    }

    @Override
    public UploadVO uploadFile(byte[] data, String relativePath, String sourceFileName, Boolean returnAbsolutePath) {
        super.checkFile(data);
        return this.getUploadVO(data, relativePath, sourceFileName, returnAbsolutePath);
    }

    @Override
    public UploadVO uploadFile(MultipartFile file, String prefix, Boolean renamed, Boolean returnAbsolutePath) {
        byte[] data = FilesUtil.getBytes(file);
        super.checkFile(data);
        String sourceFileName = file.getOriginalFilename();
        String fileName = super.getNewFileName(renamed, sourceFileName);
        String relativePath = super.getRelativePath(prefix, fileName);
        return this.getUploadVO(data, relativePath, sourceFileName, returnAbsolutePath);
    }

    @Override
    public UploadVO uploadFile(MultipartFile file, String relativePath, Boolean returnAbsolutePath) {
        byte[] data = FilesUtil.getBytes(file);
        super.checkFile(data);
        String sourceFileName = file.getOriginalFilename();
        return this.getUploadVO(data, relativePath, sourceFileName, returnAbsolutePath);
    }

    @Override
    public UploadVO uploadFile(File file, String prefix, Boolean renamed, Boolean returnAbsolutePath) {
        byte[] data = FilesUtil.getBytes(file);
        super.checkFile(data);
        String sourceFileName = file.getName();
        String fileName = super.getNewFileName(renamed, sourceFileName);
        String relativePath = super.getRelativePath(prefix, fileName);
        return this.getUploadVO(data, relativePath, sourceFileName, returnAbsolutePath);
    }

    @Override
    public UploadVO uploadFile(File file, String relativePath, Boolean returnAbsolutePath) {
        byte[] data = FilesUtil.getBytes(file);
        super.checkFile(data);
        String sourceFileName = file.getName();
        return this.getUploadVO(data, relativePath, sourceFileName, returnAbsolutePath);
    }

    @Override
    public boolean removeFiles(List<String> relativePaths) {
        String bucketName = super.getConfig().getQiniu().getBucketName();
        BucketManager bucketManager = this.getBucketManager();
        BucketManager.BatchOperations batchOperations = new BucketManager.BatchOperations();
        batchOperations.addDeleteOp(bucketName, relativePaths.toArray(String[]::new));
        try {
            Response response = bucketManager.batch(batchOperations);
            return Optional.ofNullable(response).map(Response::isOK).orElse(Boolean.FALSE);
        } catch (QiniuException ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_DELETE_ERROR);
        }
    }

    @Override
    public FilePathVO getSignUrl(String relativePath, Integer timeout) {
        return this.getUrl(relativePath, timeout);
    }

    @Override
    public List<FilePathVO> listSignUrl(List<String> relativePaths, Integer timeout, DtpExecutor taskExecutor) {
        if (CollUtil.isEmpty(relativePaths)) {
            return Collections.emptyList();
        }

        final int batchSize = OssConstant.GROUP_SIZE;
        List<List<String>> batches = Lists.partition(relativePaths, batchSize);
        List<FilePathVO> result = new ArrayList<>(relativePaths.size());

        return getFilePathList(timeout, taskExecutor, batches, result);
    }

    @Override
    public void downloadFile(HttpServletResponse response, String relativePath, String fileName) {
        FilePathVO data = this.getSignUrl(relativePath, null);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.FILE_NOT_FOUND);
        }
        try (InputStream inputStream = new BufferedInputStream(new URI(data.getAbsolutePath()).toURL().openStream())) {
            FilesUtil.downloadFile(inputStream, super.getNewFileName(relativePath, fileName), response);
        } catch (Exception ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_DOWNLOAD_ERROR);
        }
    }

    private UploadVO getUploadVO(byte[] data, String relativePath, String sourceFileName, Boolean returnAbsolutePath) {
        long fileSize = data.length;
        Response response = null;
        try {
            UploadManager uploadManager = this.getUploadManager();
            String upToken = this.getUpToken();
            response = uploadManager.put(IoUtil.toStream(data), relativePath, upToken, null, null);
            if (Objects.nonNull(response) && response.isOK()) {
                final UploadVO resultData = UploadVO.builder()
                    .sourceFileName(sourceFileName)
                    .fileName(FileUtil.getName(relativePath))
                    .fileSize(fileSize)
                    .relativePath(relativePath)
                    .build();
                if (Optional.ofNullable(returnAbsolutePath).orElse(Boolean.TRUE)) {
                    final FilePathVO filePath = this.getUrl(relativePath, OssConstant.URL_TIMEOUT);
                    resultData.setAccessDomain(filePath.getAccessDomain());
                    resultData.setAbsolutePath(filePath.getAbsolutePath());
                }
                return resultData;
            }
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_UPLOAD_ERROR);
        } finally {
            closeResponse(response);
        }
        return null;
    }

    private void closeResponse(Response response) {
        if (Objects.nonNull(response)) {
            response.close();
        }
    }

    private List<FilePathVO> getFilePathList(Integer timeout, DtpExecutor taskExecutor, List<List<String>> batches, List<FilePathVO> result) {
        for (List<String> batch : batches) {
            List<CompletableFuture<FilePathVO>> futures = batch.stream()
                .map(path -> CompletableFuture.supplyAsync(
                    () -> this.getUrl(path, timeout),
                    taskExecutor))
                .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            List<FilePathVO> batchResult = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

            result.addAll(batchResult);
        }

        return result;
    }

    private FilePathVO getUrl(String relativePath, Integer timeout) {
        String accessDomain = super.getConfig().getQiniu().getAccessDomain();
        String absolutePath = new StringBuilder(accessDomain).append(CommonConstant.SymbolConstant.SLASH).append(relativePath).toString();
        try {
            final BucketInfo bucketInfo = this.getBucketManager().getBucketInfo(super.getConfig().getQiniu().getBucketName());
            if (Objects.nonNull(bucketInfo) && bucketInfo.getPrivate() > 0) {
                Long expires = Optional.ofNullable(timeout).orElse(OssConstant.URL_TIMEOUT) * 60L;
                absolutePath = this.getAuth().privateDownloadUrl(absolutePath, expires);
            }
        } catch (QiniuException ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
        return FilePathVO.builder()
            .accessDomain(accessDomain)
            .relativePath(relativePath)
            .absolutePath(absolutePath)
            .build();
    }

    private BucketManager getBucketManager() {
        return new BucketManager(this.getAuth(), this.getConfiguration());
    }

    private UploadManager getUploadManager() throws IOException {
        Configuration cfg = this.getConfiguration();
        if (Boolean.TRUE.equals(super.getConfig().getQiniu().getBroke())) {
            Path tempPath = Paths.get(System.getenv(FileConstant.TMP_DIR), super.getConfig().getQiniu().getBucketName());
            PathUtil.mkdir(tempPath);
            // 设置断点续传文件进度保存目录
            FileRecorder fileRecorder = new FileRecorder(tempPath.toString());
            return new UploadManager(cfg, fileRecorder);
        } else {
            return new UploadManager(cfg);
        }
    }

    private Configuration getConfiguration() {
        Configuration cfg = Configuration.create(Region.autoRegion());
        // 指定分片上传版本
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;
        cfg.resumableUploadMaxConcurrentTaskCount = 5;
        return cfg;
    }

    private Auth getAuth() {
        return Auth.create(super.getConfig().getQiniu().getAccessKey(), super.getConfig().getQiniu().getSecretKey());
    }

    private String getUpToken() {
        Auth auth = this.getAuth();
        StringMap putPolicy = new StringMap();
        putPolicy.put("callbackBodyType", "application/json");
        putPolicy.put("returnBody", "{\"fileName\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"fileSize\":$(fsize)}");
        long expireSeconds = 3600L;
        return auth.uploadToken(super.getConfig().getQiniu().getBucketName(), null, expireSeconds, putPolicy);
    }
}
