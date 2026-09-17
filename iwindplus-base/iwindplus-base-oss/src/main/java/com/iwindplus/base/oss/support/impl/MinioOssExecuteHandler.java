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
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.OssTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.PreUploadVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.oss.domain.constant.OssConstant;
import com.iwindplus.base.oss.domain.constant.OssConstant.MinioConstant;
import com.iwindplus.base.oss.domain.dto.OssCloudDownloadDTO;
import com.iwindplus.base.oss.domain.dto.OssCloudGetSignUrlDTO;
import com.iwindplus.base.oss.domain.dto.OssCloudListSignUrlDTO;
import com.iwindplus.base.oss.domain.dto.OssCloudPreUploadDTO;
import com.iwindplus.base.oss.domain.dto.OssCloudRemoveDTO;
import com.iwindplus.base.oss.domain.dto.OssCloudUploadDTO;
import com.iwindplus.base.oss.domain.property.OssProperty;
import com.iwindplus.base.oss.domain.property.OssProperty.MinioConfig;
import com.iwindplus.base.oss.service.impl.AbstractOssBaseServiceImpl;
import com.iwindplus.base.oss.support.OssExecuteHandler;
import com.iwindplus.base.util.FilesUtil;
import com.iwindplus.base.util.IosUtil;
import com.iwindplus.base.util.JacksonUtil;
import io.minio.GetBucketPolicyArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Minio对象存储策略实现类.
 *
 * @author zengdegui
 * @since 2019/8/9
 */
@Slf4j
public class MinioOssExecuteHandler extends AbstractOssBaseServiceImpl<MinioConfig> implements OssExecuteHandler {

    private final ObjectProvider<OkHttpClient> okHttpClientProvider;

    /**
     * 创建 Minio OSS 策略.
     *
     * @param multipartProperties  文件上传配置
     * @param config               Minio配置
     * @param okHttpClientProvider HTTP客户端提供器
     */
    public MinioOssExecuteHandler(
        MultipartProperties multipartProperties,
        MinioConfig config,
        ObjectProvider<OkHttpClient> okHttpClientProvider) {
        super(multipartProperties);
        super.setConfig(config);
        this.okHttpClientProvider = okHttpClientProvider;
    }

    @Override
    public OssTypeEnum getProvider() {
        return OssTypeEnum.MINIO;
    }

    @Override
    public PreUploadVO preUpload(OssCloudPreUploadDTO request) {
        request.setRelativePath(this.getRelativePath(request.getRelativePath(), request.getRenamed()));

        MinioClient minioClient = null;
        try {
            minioClient = this.getMinioClient();
            Integer timeout = Optional.ofNullable(request.getTimeout()).orElse(OssConstant.URL_TIMEOUT);
            final String contentType = Optional.ofNullable(request.getContentType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            GetPresignedObjectUrlArgs.Builder builder = GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(request.getBucketName())
                .object(request.getRelativePath())
                .extraHeaders(Map.of(HttpHeaders.CONTENT_TYPE, contentType))
                .expiry(timeout, TimeUnit.MINUTES);
            String uploadUrl = minioClient.getPresignedObjectUrl(builder.build());
            return PreUploadVO.builder()
                .relativePath(request.getRelativePath())
                .uploadUrl(uploadUrl)
                .timeout(timeout)
                .build();
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_UPLOAD_ERROR);
        } finally {
            this.closeMinioClient(minioClient);
        }
    }

    @Override
    public UploadVO uploadFile(OssCloudUploadDTO request) {
        super.prepareUpload(request);
        return this.getUploadVO(request);
    }

    @Override
    public boolean removeFiles(OssCloudRemoveDTO request) {
        List<DeleteObject> objects = request.getRelativePaths().stream()
            .map(DeleteObject::new)
            .toList();

        MinioClient minioClient = null;
        try {
            minioClient = this.getMinioClient();

            Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                    .bucket(request.getBucketName())
                    .objects(objects)
                    .build()
            );

            boolean success = true;
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                if (error.code() != null) {
                    success = false;
                    log.error("MinIO 删除失败: object={}, code={}, message={}",
                        error.objectName(), error.code(), error.message());
                }
            }
            return success;
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
            throw new BizException(BizCodeEnum.FILE_DELETE_ERROR);
        } finally {
            this.closeMinioClient(minioClient);
        }
    }

    @Override
    public FilePathVO getSignUrl(OssCloudGetSignUrlDTO request) {
        MinioClient minioClient = this.getMinioClient();
        try {
            return this.getUrl(
                request.getBucketName(),
                request.getAccessDomain(),
                request.getRelativePath(),
                request.getTimeout(),
                minioClient);
        } finally {
            this.closeMinioClient(minioClient);
        }
    }

    @Override
    public List<FilePathVO> listSignUrl(OssCloudListSignUrlDTO request) {
        if (CollUtil.isEmpty(request.getRelativePaths())) {
            return Collections.emptyList();
        }

        final int batchSize = OssConstant.GROUP_SIZE;
        List<List<String>> batches = Lists.partition(request.getRelativePaths(), batchSize);
        MinioClient minioClient = this.getMinioClient();
        try {
            return getFilePathList(request, batches, minioClient);
        } finally {
            this.closeMinioClient(minioClient);
        }
    }

    @Override
    public void downloadFile(OssCloudDownloadDTO request) {
        final GetObjectArgs build = GetObjectArgs.builder()
            .bucket(request.getBucketName())
            .object(request.getRelativePath()).build();
        MinioClient minioClient = null;
        try {
            minioClient = this.getMinioClient();
            final GetObjectResponse ossObject = minioClient.getObject(build);
            FilesUtil.downloadFile(
                ossObject,
                super.getNewFileName(request.getRelativePath(), request.getFileName()),
                request.getResponse());
        } catch (Exception ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_DOWNLOAD_ERROR);
        } finally {
            this.closeMinioClient(minioClient);
        }
    }

    private UploadVO getUploadVO(OssCloudUploadDTO request) {
        request.setRelativePath(this.getRelativePath(request.getRelativePath(), request.getRenamed()));

        long fileSize = request.getData().length;
        Long partSize = (Optional.ofNullable(this.getConfig().getPartSize()).orElse(OssConstant.PART_SIZE)) * 1024 * 1024;
        MinioClient minioClient = null;
        try {
            minioClient = this.getMinioClient();
            final String contentType = Optional.ofNullable(request.getContentType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket(request.getBucketName())
                .object(request.getRelativePath())
                .stream(IoUtil.toStream(request.getData()), fileSize, partSize)
                .contentType(contentType)
                .build();
            ObjectWriteResponse objectWriteResponse = minioClient.putObject(putObjectArgs);
            if (Objects.nonNull(objectWriteResponse)) {
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
                        OssConstant.URL_TIMEOUT,
                        minioClient);
                    resultData.setAccessDomain(filePath.getAccessDomain());
                    resultData.setAbsolutePath(filePath.getAbsolutePath());
                }
                return resultData;
            }
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_UPLOAD_ERROR);
        } finally {
            this.closeMinioClient(minioClient);
        }
        return null;
    }

    private List<FilePathVO> getFilePathList(
        OssCloudListSignUrlDTO request,
        List<List<String>> batches,
        MinioClient minioClient) {

        return batches.stream()
            .flatMap(batch -> {
                final List<CompletableFuture<FilePathVO>> futures = batch.stream()
                    .map(relativePath -> CompletableFuture.supplyAsync(
                        () -> getUrl(
                            request.getBucketName(),
                            request.getAccessDomain(),
                            relativePath,
                            request.getTimeout(),
                            minioClient
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
        Integer timeout,
        MinioClient minioClient) {

        String absolutePath = null;
        if (CharSequenceUtil.isBlank(accessDomain)) {
            accessDomain = this.getConfig().getEndpoint();
        }
        try {
            // 获取存储桶策略
            String policy = minioClient.getBucketPolicy(GetBucketPolicyArgs.builder().bucket(bucketName).build());
            // 检查策略是否允许匿名访问
            boolean isPublic = this.isPublicBucket(policy);
            if (isPublic) {
                // 返回公共 URL
                absolutePath = StrUtil.format("{}/{}", accessDomain, relativePath);
            } else {
                Integer expires = Optional.ofNullable(timeout).orElse(OssConstant.URL_TIMEOUT);

                // 生成带签名的临时 URL
                absolutePath = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(relativePath)
                        .expiry(expires, TimeUnit.MINUTES)
                        .build()
                );
            }
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
        return FilePathVO.builder()
            .accessDomain(accessDomain)
            .relativePath(relativePath)
            .absolutePath(absolutePath)
            .build();
    }


    /**
     * 判断给定的 MinIO 存储桶策略是否为 public。
     *
     * @param policyJson 存储桶策略 JSON 字符串
     * @return true 表示 public，false 表示 private
     */
    private boolean isPublicBucket(String policyJson) {
        if (StrUtil.isBlank(policyJson)) {
            return false;
        }
        JsonNode rootNode = JacksonUtil.parseTree(policyJson);
        JsonNode statements = rootNode.get(MinioConstant.POLICY_STATEMENT);
        if (statements == null || statements.isEmpty()) {
            return false;
        }
        for (JsonNode statement : statements) {
            if (statement.get(MinioConstant.POLICY_EFFECT) == null
                || !statement.get(MinioConstant.POLICY_EFFECT).asText().equals(MinioConstant.POLICY_EFFECT_ALLOW)) {
                continue;
            }
            JsonNode principal = statement.get(MinioConstant.POLICY_PRINCIPAL);
            if (principal == null) {
                continue;
            }
            // 处理 Principal 为 * 或 {"AWS":"*"}
            if (principal.isTextual() && MinioConstant.POLICY_PRINCIPAL_WILDCARD.equals(principal.asText())) {
                return true;
            }
            if (principal.isObject()) {
                JsonNode aws = principal.get(MinioConstant.POLICY_PRINCIPAL_AWS);
                if (aws != null && MinioConstant.POLICY_PRINCIPAL_WILDCARD.equals(aws.asText())) {
                    return true;
                }
            }
            JsonNode actions = statement.get(MinioConstant.POLICY_ACTION);
            if (actions != null && actions.isArray()) {
                for (JsonNode action : actions) {
                    if (MinioConstant.POLICY_ACTION_GET_OBJECT.equals(action.asText())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private MinioClient getMinioClient() {
        MinioConfig config = this.getConfig();

        MinioClient.Builder builder = MinioClient.builder()
            .endpoint(config.getEndpoint())
            .credentials(config.getAccessKey(), config.getSecretKey())
            .httpClient(okHttpClientProvider.getIfAvailable());

        if (CharSequenceUtil.isNotBlank(config.getRegion())) {
            builder.region(config.getRegion());
        }

        return builder.build();
    }

    private void closeMinioClient(MinioClient minioClient) {
        IosUtil.closeQuietly(minioClient);
    }
}
