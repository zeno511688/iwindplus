/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.support.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.PathUtil;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.FileConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.OssTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.PreUploadVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.oss.domain.constant.OssConstant;
import com.iwindplus.base.oss.domain.dto.OssCloudDownloadDTO;
import com.iwindplus.base.oss.domain.dto.OssCloudGetSignUrlDTO;
import com.iwindplus.base.oss.domain.dto.OssCloudListSignUrlDTO;
import com.iwindplus.base.oss.domain.dto.OssCloudPreUploadDTO;
import com.iwindplus.base.oss.domain.dto.OssCloudRemoveDTO;
import com.iwindplus.base.oss.domain.dto.OssCloudUploadDTO;
import com.iwindplus.base.oss.domain.property.OssProperty;
import com.iwindplus.base.oss.domain.property.OssProperty.QiniuConfig;
import com.iwindplus.base.oss.service.impl.AbstractOssBaseServiceImpl;
import com.iwindplus.base.oss.support.OssExecuteHandler;
import com.iwindplus.base.util.FilesUtil;
import com.iwindplus.base.util.HttpsUtil;
import com.iwindplus.base.util.IosUtil;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.http.MediaType;

/**
 * 七牛云对象存储策略实现类.
 *
 * @author zengdegui
 * @since 2019/8/9
 */
@Slf4j
public class QiniuOssExecuteHandler extends AbstractOssBaseServiceImpl<QiniuConfig> implements OssExecuteHandler {

    /**
     * 创建七牛云 OSS 策略.
     *
     * @param multipartProperties 文件上传配置
     * @param config              七牛云配置
     */
    public QiniuOssExecuteHandler(
        MultipartProperties multipartProperties,
        QiniuConfig config) {
        super(multipartProperties);
        super.setConfig(config);
    }

    @Override
    public OssTypeEnum getProvider() {
        return OssTypeEnum.QINIU;
    }

    @Override
    public PreUploadVO preUpload(OssCloudPreUploadDTO request) {
        request.setRelativePath(this.getRelativePath(request.getRelativePath(), request.getRenamed()));

        try {
            final Integer timeout = Optional.ofNullable(request.getTimeout()).orElse(OssConstant.URL_TIMEOUT);
            final long expireSeconds = timeout * 60L;
            final String contentType = Optional.ofNullable(request.getContentType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            String uploadToken = this.getUpToken(request.getBucketName(), request.getRelativePath(), contentType, expireSeconds);
            return PreUploadVO.builder()
                .relativePath(request.getRelativePath())
                .uploadUrl(uploadToken)
                .timeout(timeout)
                .build();
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_UPLOAD_ERROR);
        }
    }

    @Override
    public UploadVO uploadFile(OssCloudUploadDTO request) {
        super.prepareUpload(request);
        return this.getUploadVO(request);
    }

    @Override
    public boolean removeFiles(OssCloudRemoveDTO request) {
        BucketManager bucketManager = this.getBucketManager();
        BucketManager.BatchOperations batchOperations = new BucketManager.BatchOperations();
        batchOperations.addDeleteOp(request.getBucketName(), request.getRelativePaths().toArray(String[]::new));
        try {
            Response response = bucketManager.batch(batchOperations);
            return Optional.ofNullable(response).map(Response::isOK).orElse(Boolean.FALSE);
        } catch (QiniuException ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_DELETE_ERROR);
        }
    }

    @Override
    public FilePathVO getSignUrl(OssCloudGetSignUrlDTO request) {
        return this.getUrl(request.getBucketName(), request.getAccessDomain(), request.getRelativePath(), request.getTimeout());
    }

    @Override
    public List<FilePathVO> listSignUrl(OssCloudListSignUrlDTO request) {
        if (CollUtil.isEmpty(request.getRelativePaths())) {
            return Collections.emptyList();
        }

        final int batchSize = OssConstant.GROUP_SIZE;
        List<List<String>> batches = Lists.partition(request.getRelativePaths(), batchSize);
        return getFilePathList(request, batches);
    }

    @Override
    public void downloadFile(OssCloudDownloadDTO request) {
        FilePathVO data = this.getUrl(request.getBucketName(), request.getAccessDomain(), request.getRelativePath(), null);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.FILE_NOT_FOUND);
        }
        final byte[] bytes = HttpsUtil.downloadBytes(data.getAbsolutePath());
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            FilesUtil.downloadFile(inputStream, super.getNewFileName(request.getRelativePath(), request.getFileName()), request.getResponse());
        } catch (IOException ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_DOWNLOAD_ERROR);
        }
    }

    private UploadVO getUploadVO(OssCloudUploadDTO request) {
        request.setRelativePath(this.getRelativePath(request.getRelativePath(), request.getRenamed()));

        long fileSize = request.getData().length;
        Response response = null;
        try {
            final String contentType = Optional.ofNullable(request.getContentType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            UploadManager uploadManager = this.getUploadManager(request);
            String upToken = this.getUpToken(request.getBucketName(), request.getRelativePath(), contentType, OssConstant.URL_TIMEOUT);
            response = uploadManager.put(IoUtil.toStream(request.getData()), request.getRelativePath(), upToken, null, null);
            if (Objects.nonNull(response) && response.isOK()) {
                final UploadVO resultData = UploadVO.builder()
                    .sourceFileName(request.getSourceFileName())
                    .fileName(FileUtil.getName(request.getRelativePath()))
                    .fileSize(fileSize)
                    .relativePath(request.getRelativePath())
                    .build();
                if (Optional.ofNullable(request.getReturnAbsolutePath()).orElse(Boolean.TRUE)) {
                    final FilePathVO filePath = this.getUrl(
                        request.getBucketName(),
                        request.getAccessDomain(),
                        request.getRelativePath(),
                        OssConstant.URL_TIMEOUT);
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
        IosUtil.closeQuietly(response, Response::close);
    }

    private List<FilePathVO> getFilePathList(
        OssCloudListSignUrlDTO request,
        List<List<String>> batches) {

        return batches.stream()
            .flatMap(batch -> {
                final List<CompletableFuture<FilePathVO>> futures = batch.stream()
                    .map(relativePath -> CompletableFuture.supplyAsync(
                        () -> getUrl(
                            request.getBucketName(),
                            request.getAccessDomain(),
                            relativePath,
                            request.getTimeout()
                        ),
                        request.getThreadPoolExecutor()
                    )).toList();

                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
                return futures.stream().map(CompletableFuture::join);
            }).toList();
    }

    private FilePathVO getUrl(
        String bucketName,
        String accessDomain,
        String relativePath,
        Integer timeout) {
        String absolutePath = new StringBuilder(accessDomain).append(CommonConstant.SymbolConstant.SLASH).append(relativePath).toString();
        try {
            final BucketInfo bucketInfo = this.getBucketManager().getBucketInfo(bucketName);
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

    private UploadManager getUploadManager(OssCloudUploadDTO request) throws IOException {
        Configuration cfg = this.getConfiguration();
        if (Boolean.TRUE.equals(this.getConfig().getBroke())) {
            Path tempPath = Paths.get(System.getenv(FileConstant.TMP_DIR), request.getBucketName());
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
        return Auth.create(this.getConfig().getAccessKey(), this.getConfig().getSecretKey());
    }

    private String getUpToken(String bucketName, String relativePath, String contentType, long expireSeconds) {
        Auth auth = this.getAuth();
        StringMap policy = new StringMap();
        policy.put("callbackBodyType", "application/json");
        policy.put("returnBody", "{\"fileName\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"fileSize\":$(fsize)}");
        policy.put("mimeLimit", contentType);
        return auth.uploadToken(bucketName, relativePath, expireSeconds, policy);
    }
}
