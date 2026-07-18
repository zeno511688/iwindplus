/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.comm.Protocol;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.CompleteMultipartUploadRequest;
import com.aliyun.oss.model.CompleteMultipartUploadResult;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.InitiateMultipartUploadRequest;
import com.aliyun.oss.model.InitiateMultipartUploadResult;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PartETag;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyun.oss.model.UploadFileRequest;
import com.aliyun.oss.model.UploadFileResult;
import com.aliyun.oss.model.UploadPartRequest;
import com.aliyun.oss.model.UploadPartResult;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.google.common.collect.Lists;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.oss.domain.constant.OssConstant;
import com.iwindplus.base.oss.domain.dto.StsTokenDTO;
import com.iwindplus.base.oss.domain.property.OssProperty;
import com.iwindplus.base.oss.service.OssAliyunService;
import com.iwindplus.base.util.DatesUtil;
import com.iwindplus.base.util.FilesUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 阿里云对象存储业务层接口实现类.
 *
 * @author zengdegui
 * @since 2019/8/9
 */
@Slf4j
public class OssAliyunServiceImpl extends AbstractOssBaseServiceImpl implements OssAliyunService {

    @Override
    public UploadVO uploadFile(byte[] data, String prefix, String sourceFileName, Boolean renamed, Boolean returnAbsolutePath) {
        super.checkFile(data);
        String fileName = super.getNewFileName(renamed, sourceFileName);
        String relativePath = this.getRelativePath(prefix, fileName);
        return this.getUploadVO(data, relativePath, sourceFileName, returnAbsolutePath);
    }

    @Override
    public UploadVO uploadFile(byte[] data, String relativePath, String sourceFileName, Boolean returnAbsolutePath) {
        super.checkFile(data);
        return this.getUploadVO(data, relativePath, sourceFileName, returnAbsolutePath);
    }

    @Override
    public UploadVO uploadFile(MultipartFile file, String prefix, Boolean renamed, Boolean returnAbsolutePath) {
        super.checkFile(file);
        byte[] data = FilesUtil.getBytes(file);
        String sourceFileName = file.getOriginalFilename();
        String fileName = super.getNewFileName(renamed, sourceFileName);
        String relativePath = super.getRelativePath(prefix, fileName);
        return this.getUploadVO(data, relativePath, sourceFileName, returnAbsolutePath);
    }

    @Override
    public UploadVO uploadFile(MultipartFile file, String relativePath, Boolean returnAbsolutePath) {
        super.checkFile(file);
        byte[] data = FilesUtil.getBytes(file);
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
    public FilePathVO getSignUrl(String relativePath, Integer timeout) {
        OSS ossClient = null;
        try {
            ossClient = this.getOssClient();
            return this.getUrl(ossClient, relativePath, timeout);
        } finally {
            this.closeOssClient(ossClient);
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

        OSS ossClient = this.getOssClient();
        try {
            return getFilePathList(timeout, taskExecutor, batches, result, ossClient);
        } finally {
            this.closeOssClient(ossClient);
        }
    }

    @Override
    public boolean removeFiles(List<String> relativePaths) {
        OSS ossClient = null;
        try {
            ossClient = this.getOssClient();
            String bucketName = super.getConfig().getAliyun().getBucketName();
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName);
            deleteObjectsRequest.setKeys(relativePaths);
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
    public void downloadFile(HttpServletResponse response, String relativePath, String fileName) {
        OSS ossClient = null;
        try {
            ossClient = this.getOssClient();
            OSSObject ossObject = ossClient.getObject(super.getConfig().getAliyun().getBucketName(), relativePath);
            FilesUtil.downloadFile(ossObject.getObjectContent(), super.getNewFileName(relativePath, fileName), response);
        } catch (Exception ex) {
            log.error(ExceptionConstant.IO_EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_DOWNLOAD_ERROR);
        } finally {
            this.closeOssClient(ossClient);
        }
    }

    private UploadVO getUploadVO(byte[] data, String relativePath, String sourceFileName, Boolean returnAbsolutePath) {
        OSS ossClient = null;
        try {
            ossClient = this.getOssClient();
            long fileSize = data.length;
            boolean result = this.getUploadResult(ossClient, data, relativePath);
            if (Boolean.TRUE.equals(result)) {
                final UploadVO resultData = UploadVO.builder()
                    .sourceFileName(sourceFileName)
                    .fileName(FileUtil.getName(relativePath))
                    .fileSize(fileSize)
                    .relativePath(relativePath)
                    .build();
                if (Optional.ofNullable(returnAbsolutePath).orElse(Boolean.TRUE)) {
                    final FilePathVO filePath = this.getUrl(ossClient, relativePath, OssConstant.URL_TIMEOUT);
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

    private boolean getUploadResult(OSS ossClient, byte[] data, String relativePath) throws Throwable {
        long fileSize = data.length;
        if (fileSize < CommonConstant.FileConstant.FILE_SIZE) {
            return this.getUploadBySimple(ossClient, data, relativePath);
        }
        if (Objects.nonNull(super.getConfig().getAliyun().getBroke()) && Boolean.TRUE.equals(super.getConfig().getAliyun().getBroke())) {
            return this.getUploadByBreakpoint(ossClient, data, relativePath);
        }
        return this.getUploadByPart(ossClient, data, relativePath);
    }

    private boolean getUploadBySimple(OSS ossClient, byte[] data, String relativePath) {
        PutObjectResult response = ossClient.putObject(super.getConfig().getAliyun().getBucketName(), relativePath, new ByteArrayInputStream(data));
        return Objects.nonNull(response);
    }

    private boolean getUploadByBreakpoint(OSS ossClient, byte[] data, String relativePath) throws Throwable {
        String rootPath = this.getRootPath();
        Path absolutePath = Paths.get(rootPath).resolve(relativePath);
        File tempFile = FileUtil.writeBytes(data, absolutePath.toString());
        String uploadFile = tempFile.getAbsolutePath();
        try {
            Long partSize =
                (Optional.ofNullable(super.getConfig().getAliyun()).map(OssProperty.AliyunConfig::getPartSize).orElse(OssConstant.PART_SIZE)) * 1024
                    * 1024;
            UploadFileRequest uploadFileRequest = new UploadFileRequest(super.getConfig().getAliyun().getBucketName(), relativePath, uploadFile,
                partSize, 5, true);
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

    private boolean getUploadByPart(OSS ossClient, byte[] data, String relativePath) throws IOException {
        // 创建InitiateMultipartUploadRequest对象。
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(super.getConfig().getAliyun().getBucketName(), relativePath);
        // 初始化分片。
        InitiateMultipartUploadResult upResult = ossClient.initiateMultipartUpload(request);
        // 返回uploadId，它是分片上传事件的唯一标识，您可以根据这个uploadId发起相关的操作，如取消分片上传、查询分片上传等。
        String uploadId = upResult.getUploadId();
        // 返回uploadId，它是分片上传事件的唯一标识，可以根据这个ID来发起相关操作，如取消分片上传、查询分片上传等
        List<PartETag> partTags = listPartEtag(ossClient, data, relativePath, uploadId);
        // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
        CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest(
            super.getConfig().getAliyun().getBucketName(), relativePath, uploadId, partTags);
        // 完成上传。
        CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
        return Objects.nonNull(completeMultipartUploadResult);
    }

    private List<PartETag> listPartEtag(OSS ossClient, byte[] data, String relativePath, String uploadId) throws IOException {
        // 每个分片的大小，用于计算文件有多少个分片。单位为字节。
        long fileSize = data.length;
        Long partSize =
            (Optional.ofNullable(super.getConfig().getAliyun()).map(OssProperty.AliyunConfig::getPartSize).orElse(OssConstant.PART_SIZE)) * 1024
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
            uploadPartRequest.setBucketName(super.getConfig().getAliyun().getBucketName());
            uploadPartRequest.setKey(relativePath);
            uploadPartRequest.setUploadId(uploadId);
            try (InputStream inputStream = IoUtil.toStream(data)) {
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

    private List<FilePathVO> getFilePathList(Integer timeout, DtpExecutor taskExecutor, List<List<String>> batches, List<FilePathVO> result,
        OSS ossClient) {
        for (List<String> batch : batches) {
            List<CompletableFuture<FilePathVO>> futures = batch.stream()
                .map(path -> CompletableFuture.supplyAsync(
                    () -> this.getUrl(ossClient, path, timeout),
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

    private FilePathVO getUrl(OSS ossClient, String relativePath, Integer timeout) {
        OssProperty.AliyunConfig cfg = super.getConfig().getAliyun();
        String bucket = cfg.getBucketName();
        String endpoint = cfg.getEndpoint();
        String absolutePath;
        String accessDomainOss = new StringBuilder(CommonConstant.NetWorkConstant.HTTPS_PREFIX)
            .append(bucket)
            .append(CommonConstant.SymbolConstant.POINT).append(endpoint).toString();
        String accessDomain = super.getConfig().getAliyun().getAccessDomain();
        if (CharSequenceUtil.isBlank(accessDomain)) {
            accessDomain = accessDomainOss;
        }
        final boolean isPrivate = ossClient.getBucketAcl(bucket).getCannedACL() == CannedAccessControlList.Private;
        // 判断是否是私有空间
        if (isPrivate) {
            Date expiration = DateUtil.offsetMinute(new Date(), Optional.ofNullable(timeout).orElse(OssConstant.URL_TIMEOUT)).toJdkDate();
            absolutePath = ossClient.generatePresignedUrl(bucket, relativePath, expiration).toString();
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

    private OSS getOssClient() {
        ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
        conf.setSupportCname(true);
        conf.setProtocol(Protocol.HTTPS);
        final OssProperty.AliyunConfig aliyun = super.getConfig().getAliyun();
        final StsTokenDTO sts = aliyun.getSts();
        if (Objects.nonNull(sts)) {
            final LocalDateTime securityTokenExpiration = sts.getExpiration();
            if (Objects.isNull(securityTokenExpiration) || LocalDateTime.now().isAfter(securityTokenExpiration)) {
                AssumeRoleResponse response = this.getAssumeRoleResponse(aliyun, sts);
                final LocalDateTime expiration = DatesUtil.parseUtcDate(response.getCredentials().getExpiration());
                sts.setAccessKey(response.getCredentials().getAccessKeyId());
                sts.setSecretKey(response.getCredentials().getAccessKeySecret());
                sts.setSecurityToken(response.getCredentials().getSecurityToken());
                sts.setExpiration(expiration);
            }
            return new OSSClientBuilder().build(aliyun.getEndpoint(), sts.getAccessKey(), sts.getSecretKey(), sts.getSecurityToken(), conf);
        }
        return new OSSClientBuilder().build(aliyun.getEndpoint(), aliyun.getAccessKey(), aliyun.getSecretKey(), conf);
    }

    private void closeOssClient(OSS ossClient) {
        if (Objects.nonNull(ossClient)) {
            ossClient.shutdown();
        }
    }

}
