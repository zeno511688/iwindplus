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
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.oss.domain.constant.OssConstant;
import com.iwindplus.base.oss.domain.property.OssProperty;
import com.iwindplus.base.oss.service.OssMinioService;
import com.iwindplus.base.util.FilesUtil;
import com.iwindplus.base.util.JacksonUtil;
import io.minio.GetBucketPolicyArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.MinioClient.Builder;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

/**
 * Minio对象存储操作业务层接口实现类.
 *
 * @author zengdegui
 * @since 2019/8/9
 */
@Slf4j
public class OssMinioServiceImpl extends AbstractOssBaseServiceImpl implements OssMinioService {

    @Autowired(required = false)
    private ObjectProvider<OkHttpClient> okHttpClientProvider;

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
        List<DeleteObject> deleteObjects = relativePaths.stream()
            .parallel().map(m -> new DeleteObject(m)).collect(Collectors.toList());
        RemoveObjectsArgs build = RemoveObjectsArgs.builder()
            .bucket(super.getConfig().getMinio().getBucketName())
            .objects(deleteObjects)
            .build();
        MinioClient minioClient = null;
        try {
            minioClient = this.getMinioClient();
            final Iterable<Result<DeleteError>> results = minioClient.removeObjects(build);
            for (Result<DeleteError> result : results){
                result.get();
            }
            return Boolean.TRUE;
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_DELETE_ERROR);
        } finally {
            this.closeMinioClient(minioClient);
        }
    }

    @Override
    public FilePathVO getSignUrl(String relativePath, Integer timeout) {
        MinioClient minioClient = this.getMinioClient();
        try {
            return this.getUrl(minioClient, relativePath, timeout);
        } finally {
            this.closeMinioClient(minioClient);
        }
    }

    @Override
    public List<FilePathVO> listSignUrl(List<String> relativePaths, Integer timeout, DtpExecutor taskExecutor) {
        if (CollUtil.isEmpty(relativePaths)) {
            return Collections.emptyList();
        }

        final int batchSize = OssConstant.GROUP_SIZE;
        List<List<String>> batches = Lists.partition(relativePaths, batchSize);
        List<FilePathVO> result = new ArrayList<>(relativePaths.size());

        MinioClient minioClient = this.getMinioClient();
        try {
            return getFilePathList(timeout, taskExecutor, batches, result, minioClient);
        } finally {
            this.closeMinioClient(minioClient);
        }
    }

    @Override
    public void downloadFile(HttpServletResponse response, String relativePath, String fileName) {
        FilePathVO data = this.getSignUrl(relativePath, null);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.FILE_NOT_FOUND);
        }
        final GetObjectArgs build = GetObjectArgs.builder()
            .bucket(super.getConfig().getMinio().getBucketName())
            .object(relativePath).build();
        MinioClient minioClient = null;
        try {
            minioClient = this.getMinioClient();
            final GetObjectResponse ossObject = minioClient.getObject(build);
            FilesUtil.downloadFile(ossObject, super.getNewFileName(relativePath, fileName), response);
        } catch (Exception ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_DOWNLOAD_ERROR);
        } finally {
            this.closeMinioClient(minioClient);
        }
    }

    private UploadVO getUploadVO(byte[] data, String relativePath, String sourceFileName, Boolean returnAbsolutePath) {
        long fileSize = data.length;
        Long partSize =
            (Optional.ofNullable(super.getConfig().getMinio()).map(OssProperty.MinioConfig::getPartSize).orElse(OssConstant.PART_SIZE)) * 1024 * 1024;
        MinioClient minioClient = null;
        try {
            minioClient = this.getMinioClient();
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket(super.getConfig().getMinio().getBucketName())
                .object(relativePath)
                .stream(IoUtil.toStream(data), data.length, partSize)
                .build();
            ObjectWriteResponse objectWriteResponse = minioClient.putObject(putObjectArgs);
            if (Objects.nonNull(objectWriteResponse)) {
                final UploadVO resultData = UploadVO.builder()
                    .sourceFileName(sourceFileName)
                    .fileName(FileUtil.getName(relativePath))
                    .fileSize(fileSize)
                    .relativePath(relativePath)
                    .build();
                if (Optional.ofNullable(returnAbsolutePath).orElse(Boolean.TRUE)) {
                    final FilePathVO filePath = this.getUrl(minioClient, relativePath, OssConstant.URL_TIMEOUT);
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

    private List<FilePathVO> getFilePathList(Integer timeout, DtpExecutor taskExecutor, List<List<String>> batches, List<FilePathVO> result,
        MinioClient minioClient) {
        for (List<String> batch : batches) {
            List<CompletableFuture<FilePathVO>> futures = batch.stream()
                .map(path -> CompletableFuture.supplyAsync(
                    () -> this.getUrl(minioClient, path, timeout),
                    taskExecutor))
                .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            List<FilePathVO> batchResult = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

            result.addAll(batchResult);
        }

        log.info("getFilePathList result: {}", result);

        return result;
    }

    private FilePathVO getUrl(MinioClient minioClient, String relativePath, Integer timeout) {
        String absolutePath = null;
        String bucketName = super.getConfig().getMinio().getBucketName();
        String accessDomain = super.getConfig().getMinio().getAccessDomain();
        if (CharSequenceUtil.isBlank(accessDomain)) {
            accessDomain = super.getConfig().getMinio().getEndpoint();
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
        JsonNode rootNode = JacksonUtil.parseTree(policyJson);
        if (rootNode == null) {
            return false;
        }

        JsonNode statementsNode = rootNode.get("Statement");

        if (statementsNode == null || !statementsNode.isArray()) {
            return false;
        }

        for (JsonNode statement : statementsNode) {
            JsonNode principalNode = statement.get("Principal");
            if (principalNode == null || !principalNode.isObject()) {
                continue;
            }

            JsonNode awsNode = principalNode.get("AWS");
            if (awsNode == null) {
                continue;
            }

            boolean isAnonymous = awsNode.isTextual() && "*".equals(awsNode.asText())
                || awsNode.isArray() && hasWildcard(awsNode);

            if (!isAnonymous) {
                continue;
            }

            JsonNode actionNode = statement.get("Action");
            if (null != actionNode && actionNode.isArray() && containsGetObject(actionNode)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasWildcard(JsonNode awsNode) {
        for (JsonNode node : awsNode) {
            if ("*".equals(node.asText())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsGetObject(JsonNode actionNode) {
        Iterator<JsonNode> elements = actionNode.elements();
        while (elements.hasNext()) {
            if ("s3:GetObject".equals(elements.next().asText())) {
                return true;
            }
        }
        return false;
    }

    private MinioClient getMinioClient() {
        OssProperty.MinioConfig minio = super.getConfig().getMinio();
        final Builder builder = MinioClient.builder()
            .endpoint(minio.getEndpoint())
            .credentials(minio.getAccessKey(), minio.getSecretKey())
            .region(minio.getRegion())
            .httpClient(okHttpClientProvider.getIfAvailable());
       // okHttpClientProvider.ifAvailable(builder::httpClient);
        return builder.build();
    }

    private void closeMinioClient(MinioClient minioClient) {
        if (Objects.nonNull(minioClient)) {
            try {
                minioClient.close();
            } catch (Exception ex) {
                log.error(ExceptionConstant.IO_EXCEPTION, ex);
            }
        }
    }
}
