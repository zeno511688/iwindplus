/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.support.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.comm.Protocol;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.CompleteMultipartUploadRequest;
import com.aliyun.oss.model.CompleteMultipartUploadResult;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.InitiateMultipartUploadRequest;
import com.aliyun.oss.model.InitiateMultipartUploadResult;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PartETag;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyun.oss.model.UploadFileRequest;
import com.aliyun.oss.model.UploadFileResult;
import com.aliyun.oss.model.UploadPartRequest;
import com.aliyun.oss.model.UploadPartResult;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
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
import com.iwindplus.base.domain.dto.StsTokenDTO;
import com.iwindplus.base.oss.domain.property.OssProperty;
import com.iwindplus.base.oss.domain.property.OssProperty.AliyunConfig;
import com.iwindplus.base.oss.service.impl.AbstractOssBaseServiceImpl;
import com.iwindplus.base.oss.support.OssExecuteHandler;
import com.iwindplus.base.util.FilesUtil;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.http.MediaType;

/**
 * 阿里云对象存储策略实现类.
 *
 * @author zengdegui
 * @since 2019/8/9
 */
@Slf4j
public class AliyunOssExecuteHandler extends AbstractOssBaseServiceImpl<AliyunConfig> implements OssExecuteHandler {

    /**
     * 创建阿里云 OSS 策略.
     *
     * @param multipartProperties 文件上传配置
     * @param config              阿里云配置
     */
    public AliyunOssExecuteHandler(
        MultipartProperties multipartProperties,
        OssProperty.AliyunConfig config) {
        super(multipartProperties);
        super.setConfig(config);
    }

    @Override
    public OssTypeEnum getProvider() {
        return OssTypeEnum.ALIYUN;
    }

    @Override
    public PreUploadVO preUpload(OssCloudPreUploadDTO request) {
        request.setRelativePath(this.getRelativePath(request.getRelativePath(), request.getRenamed()));

        OSS ossClient = null;
        try {
            ossClient = this.getOssClient();
            final Integer timeout = Optional.ofNullable(request.getTimeout()).orElse(OssConstant.URL_TIMEOUT);
            Date expiration = DateUtil.offsetMinute(new Date(), timeout).toJdkDate();
            GeneratePresignedUrlRequest preSignedUrlRequest = new GeneratePresignedUrlRequest(
                request.getBucketName(), request.getRelativePath(), HttpMethod.PUT);
            preSignedUrlRequest.setExpiration(expiration);
            final String contentType = Optional.ofNullable(request.getContentType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            preSignedUrlRequest.setContentType(contentType);
            String uploadUrl = ossClient.generatePresignedUrl(preSignedUrlRequest).toString();
            String accessDomain = this.resolveAccessDomain(request.getBucketName(), request.getAccessDomain());
            if (CharSequenceUtil.isNotBlank(request.getAccessDomain())) {
                uploadUrl = CharSequenceUtil.replace(uploadUrl, accessDomain, request.getAccessDomain());
            }
            return PreUploadVO.builder()
                .relativePath(request.getRelativePath())
                .uploadUrl(uploadUrl)
                .timeout(timeout)
                .build();
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_UPLOAD_ERROR);
        } finally {
            this.closeOssClient(ossClient);
        }
    }

    @Override
    public UploadVO uploadFile(OssCloudUploadDTO request) {
        super.prepareUpload(request);
        return this.getUploadVO(request);
    }

    @Override
    public FilePathVO getSignUrl(OssCloudGetSignUrlDTO request) {
        OSS ossClient = null;
        try {
            ossClient = this.getOssClient();
            return this.getUrl(
                request.getBucketName(),
                request.getAccessDomain(),
                request.getRelativePath(),
                request.getTimeout(),
                ossClient);
        } finally {
            this.closeOssClient(ossClient);
        }
    }

    @Override
    public List<FilePathVO> listSignUrl(OssCloudListSignUrlDTO request) {
        if (CollUtil.isEmpty(request.getRelativePaths())) {
            return Collections.emptyList();
        }

        final int batchSize = OssConstant.GROUP_SIZE;
        List<List<String>> batches = Lists.partition(request.getRelativePaths(), batchSize);
        OSS ossClient = this.getOssClient();
        try {
            return getFilePathList(request, batches, ossClient);
        } finally {
            this.closeOssClient(ossClient);
        }
    }

    @Override
    public boolean removeFiles(OssCloudRemoveDTO request) {
        OSS ossClient = null;
        try {
            ossClient = this.getOssClient();
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(request.getBucketName());
            deleteObjectsRequest.setKeys(request.getRelativePaths());
            ossClient.deleteObjects(deleteObjectsRequest);
            return true;
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_DELETE_ERROR);
        } finally {
            this.closeOssClient(ossClient);
        }
    }

    @Override
    public void downloadFile(OssCloudDownloadDTO request) {
        OSS ossClient = null;
        try {
            ossClient = this.getOssClient();
            OSSObject ossObject = ossClient.getObject(request.getBucketName(), request.getRelativePath());
            FilesUtil.downloadFile(
                ossObject.getObjectContent(),
                super.getNewFileName(request.getRelativePath(), request.getFileName()),
                request.getResponse());
        } catch (Exception ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_DOWNLOAD_ERROR);
        } finally {
            this.closeOssClient(ossClient);
        }
    }

    private UploadVO getUploadVO(OssCloudUploadDTO request) {
        request.setRelativePath(this.getRelativePath(request.getRelativePath(), request.getRenamed()));

        OSS ossClient = null;
        try {
            ossClient = this.getOssClient();
            long fileSize = request.getData().length;
            final String contentType = Optional.ofNullable(request.getContentType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            request.setContentType(contentType);
            boolean result = this.getUploadResult(request, ossClient);
            if (Boolean.TRUE.equals(result)) {
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
                        ossClient);
                    resultData.setAccessDomain(filePath.getAccessDomain());
                    resultData.setAbsolutePath(filePath.getAbsolutePath());
                }
                return resultData;
            }
        } catch (Throwable ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_UPLOAD_ERROR);
        } finally {
            this.closeOssClient(ossClient);
        }
        return null;
    }

    private boolean getUploadResult(OssCloudUploadDTO request, OSS ossClient) throws Throwable {
        long fileSize = request.getData().length;
        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(request.getContentType());
        metadata.setContentLength(request.getData().length);

        if (fileSize < CommonConstant.FileConstant.FILE_SIZE) {
            return this.getUploadBySimple(request, metadata, ossClient);
        }
        if (Objects.nonNull(this.getConfig().getBroke()) && Boolean.TRUE.equals(this.getConfig().getBroke())) {
            return this.getUploadByBreakpoint(request, metadata, ossClient);
        }
        return this.getUploadByPart(request, metadata, ossClient);
    }

    private boolean getUploadBySimple(OssCloudUploadDTO request, ObjectMetadata metadata, OSS ossClient) {
        PutObjectResult response = ossClient.putObject(
            request.getBucketName(),
            request.getRelativePath(),
            new ByteArrayInputStream(request.getData()),
            metadata);
        return Objects.nonNull(response);
    }

    private boolean getUploadByBreakpoint(OssCloudUploadDTO request, ObjectMetadata metadata, OSS ossClient) throws Throwable {
        String rootPath = this.getRootPath();
        Path absolutePath = Paths.get(rootPath).resolve(request.getRelativePath());
        File tempFile = FileUtil.writeBytes(request.getData(), absolutePath.toString());
        String uploadFile = tempFile.getAbsolutePath();
        Long partSize = (Optional.ofNullable(this.getConfig().getPartSize())
            .orElse(OssConstant.PART_SIZE)) * 1024 * 1024;

        UploadFileRequest uploadFileRequest = new UploadFileRequest(
            request.getBucketName(), request.getRelativePath(), uploadFile,
            partSize, 5, true);
        uploadFileRequest.setObjectMetadata(metadata);
        try {
            UploadFileResult uploadFileResult = ossClient.uploadFile(uploadFileRequest);
            if (Objects.nonNull(uploadFileResult)) {
                return true;
            }
        } finally {
            if (FileUtil.exist(tempFile)) {
                FileUtil.del(tempFile);
            }
        }
        return false;
    }

    private boolean getUploadByPart(OssCloudUploadDTO request, ObjectMetadata metadata, OSS ossClient) throws IOException {
        // 创建InitiateMultipartUploadRequest对象。
        final InitiateMultipartUploadRequest uploadRequest =
            new InitiateMultipartUploadRequest(request.getBucketName(), request.getRelativePath());
        uploadRequest.setObjectMetadata(metadata);
        // 初始化分片。
        InitiateMultipartUploadResult upResult = ossClient.initiateMultipartUpload(uploadRequest);
        // 返回uploadId，它是分片上传事件的唯一标识，您可以根据这个uploadId发起相关的操作，如取消分片上传、查询分片上传等。
        String uploadId = upResult.getUploadId();
        // 返回uploadId，它是分片上传事件的唯一标识，可以根据这个ID来发起相关操作，如取消分片上传、查询分片上传等
        List<PartETag> partTags = listPartEtag(request, ossClient, uploadId);
        // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
        CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest(
            request.getBucketName(), request.getRelativePath(), uploadId, partTags);
        // 完成上传。
        CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
        return Objects.nonNull(completeMultipartUploadResult);
    }

    private List<PartETag> listPartEtag(OssCloudUploadDTO request, OSS ossClient, String uploadId)
        throws IOException {
        // 每个分片的大小，用于计算文件有多少个分片。单位为字节。
        long fileSize = request.getData().length;
        Long partSize =
            (Optional.ofNullable(this.getConfig().getPartSize()).orElse(OssConstant.PART_SIZE)) * 1024
                * 1024;
        long partCountLong = (fileSize / partSize);
        if (fileSize % partSize != 0) {
            partCountLong++;
        }
        if (partCountLong > CommonConstant.FileConstant.PART_COUNT) {
            throw new BizException(BizCodeEnum.FILE_PART_TOO_BIG, new Object[]{CommonConstant.FileConstant.PART_COUNT});
        }
        int partCount = (int) partCountLong;
        List<PartETag> partTags = new ArrayList<>(10);
        for (int ii = 0; ii < partCount; ii++) {
            long startPos = ii * partSize;
            long curPartSize = (ii + 1 == partCount) ? (fileSize - startPos) : partSize;
            UploadPartRequest uploadPartRequest = new UploadPartRequest();
            uploadPartRequest.setBucketName(request.getBucketName());
            uploadPartRequest.setKey(request.getRelativePath());
            uploadPartRequest.setUploadId(uploadId);
            try (InputStream inputStream = IoUtil.toStream(request.getData())) {
                // 跳过已经上传的分片
                long skip = inputStream.skip(startPos);
                if (log.isInfoEnabled()) {
                    log.info("跳过已经上传的分片={}", skip);
                }
                uploadPartRequest.setInputStream(inputStream);
                // 设置分片大小。除了最后一个分片没有大小限制，其他分片最小为100KB
                uploadPartRequest.setPartSize(curPartSize);
                // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出这个范围,OSS将返回InvalidArgum的错误码
                uploadPartRequest.setPartNumber(ii + 1);
                // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会根据分片号排序组成完整的文件。
                UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                // 每次上传分片之后，OSS的返回结果会包含一个PartETag。PartETag将被保存到PartETags中。
                partTags.add(uploadPartResult.getPartETag());
            }
        }
        return partTags;
    }

    private List<FilePathVO> getFilePathList(
        OssCloudListSignUrlDTO request,
        List<List<String>> batches,
        OSS ossClient) {

        return batches.stream()
            .flatMap(batch -> {
                final List<CompletableFuture<FilePathVO>> futures = batch.stream()
                    .map(relativePath -> CompletableFuture.supplyAsync(
                        () -> getUrl(
                            request.getBucketName(),
                            request.getAccessDomain(),
                            relativePath,
                            request.getTimeout(),
                            ossClient
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
        OSS ossClient) {

        String absolutePath;
        String accessDomainOss = this.resolveAccessDomain(bucketName, accessDomain);
        if (CharSequenceUtil.isBlank(accessDomain)) {
            accessDomain = accessDomainOss;
        }
        final boolean isPrivate = ossClient.getBucketAcl(bucketName).getCannedACL() == CannedAccessControlList.Private;
        // 判断是否是私有空间
        if (isPrivate) {
            Date expiration = DateUtil.offsetMinute(new Date(), Optional.ofNullable(timeout).orElse(OssConstant.URL_TIMEOUT)).toJdkDate();
            absolutePath = ossClient.generatePresignedUrl(bucketName, relativePath, expiration).toString();
        } else {
            absolutePath = new StringBuilder(accessDomain).append(CommonConstant.SymbolConstant.SLASH).append(relativePath).toString();
        }
        if (!accessDomain.equals(accessDomainOss)) {
            absolutePath = CharSequenceUtil.replace(absolutePath, accessDomainOss, accessDomain);
        }
        return FilePathVO.builder()
            .accessDomain(accessDomain)
            .relativePath(relativePath)
            .absolutePath(absolutePath)
            .build();
    }

    /**
     * 解析OSS访问域名（默认使用 https://bucketName.endpoint）.
     *
     * @param bucketName   空间名
     * @param accessDomain 自定义访问域名
     * @return 访问域名
     */
    private String resolveAccessDomain(String bucketName, String accessDomain) {
        if (CharSequenceUtil.isNotBlank(accessDomain)) {
            return accessDomain;
        }
        final OssProperty.AliyunConfig cfg = this.getConfig();
        return new StringBuilder(CommonConstant.NetWorkConstant.HTTPS_PREFIX)
            .append(bucketName)
            .append(CommonConstant.SymbolConstant.POINT).append(cfg.getEndpoint()).toString();
    }

    private OSS getOssClient() {
        ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
        conf.setSupportCname(true);
        conf.setProtocol(Protocol.HTTPS);
        final OssProperty.AliyunConfig config = this.getConfig();
        final StsTokenDTO sts = config.getSts();
        if (Objects.nonNull(sts) && Boolean.TRUE.equals(sts.getEnabled())) {
            refreshStsTokenIfNeeded(config, sts);
            return new OSSClientBuilder().build(config.getEndpoint(), sts.getAccessKey(), sts.getSecretKey(), sts.getSecurityToken(), conf);
        }
        return new OSSClientBuilder().build(config.getEndpoint(), config.getAccessKey(), config.getSecretKey(), conf);
    }

    private void closeOssClient(OSS ossClient) {
        if (Objects.nonNull(ossClient)) {
            ossClient.shutdown();
        }
    }

}
