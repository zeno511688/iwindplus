/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.vod.model.v20170321.CreateAuditRequest;
import com.aliyuncs.vod.model.v20170321.CreateUploadVideoRequest;
import com.aliyuncs.vod.model.v20170321.CreateUploadVideoResponse;
import com.aliyuncs.vod.model.v20170321.DeleteVideoRequest;
import com.aliyuncs.vod.model.v20170321.DeleteVideoResponse;
import com.aliyuncs.vod.model.v20170321.GetMezzanineInfoRequest;
import com.aliyuncs.vod.model.v20170321.GetMezzanineInfoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoInfoRequest;
import com.aliyuncs.vod.model.v20170321.GetVideoInfoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoPlayAuthRequest;
import com.aliyuncs.vod.model.v20170321.GetVideoPlayAuthResponse;
import com.aliyuncs.vod.model.v20170321.SubmitAIMediaAuditJobRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.dto.AkSkDTO;
import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.comm.Protocol;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.domain.vo.UploadVideoVO;
import com.iwindplus.base.oss.domain.constant.OssConstant;
import com.iwindplus.base.oss.domain.property.VodProperty;
import com.iwindplus.base.oss.service.OssAliyunService;
import com.iwindplus.base.oss.service.VodAliyunService;
import com.iwindplus.base.util.FilesUtil;
import com.iwindplus.base.util.JacksonUtil;
import jakarta.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * 阿里云视频点播业务层接口实现类.
 *
 * @author zengdegui
 * @since 2022/1/14
 */
@Slf4j
public class VodAliyunServiceImpl extends AbstractVodBaseServiceImpl implements VodAliyunService {

    @Resource
    private OssAliyunService ossAliyunService;

    @Override
    public UploadVideoVO uploadVideo(MultipartFile file) {
        byte[] data = FilesUtil.getBytes(file);
        super.checkFile(data);
        String sourceFileName = file.getOriginalFilename();
        try {
            return this.getUploadVideoVO(data, sourceFileName);
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_UPLOAD_ERROR);
        }
    }

    @Override
    public UploadVideoVO uploadVideo(File file) {
        byte[] data = FilesUtil.getBytes(file);
        super.checkFile(data);
        String sourceFileName = file.getName();
        try {
            return this.getUploadVideoVO(data, sourceFileName);
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.FILE_UPLOAD_ERROR);
        }
    }

    @Override
    public String getPlayAuth(String videoId, Long timeout) {
        DefaultAcsClient acsClient = null;
        try {
            acsClient = this.initVodClient();
            GetVideoPlayAuthRequest request = new GetVideoPlayAuthRequest();
            request.setVideoId(videoId);
            request.setAuthInfoTimeout(Optional.ofNullable(timeout).orElse(OssConstant.PLAY_AUTH_TIMEOUT * 60L));
            GetVideoPlayAuthResponse response = acsClient.getAcsResponse(request);
            if (Objects.nonNull(response)) {
                return response.getPlayAuth();
            }
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.GET_PLAY_AUTH_FAILED);
        } finally {
            this.closeAcsClient(acsClient);
        }
        return null;
    }

    @Override
    public GetVideoInfoResponse.Video getVideoInfo(String videoId) {
        DefaultAcsClient acsClient = null;
        try {
            acsClient = this.initVodClient();
            GetVideoInfoRequest request = new GetVideoInfoRequest();
            request.setVideoId(videoId);
            GetVideoInfoResponse response = acsClient.getAcsResponse(request);
            if (Objects.nonNull(response)) {
                return response.getVideo();
            }
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.GET_VIDEO_FAILED);
        } finally {
            this.closeAcsClient(acsClient);
        }
        return null;
    }

    @Override
    public GetMezzanineInfoResponse.Mezzanine getSourceVideoInfo(String videoId) {
        DefaultAcsClient acsClient = null;
        try {
            acsClient = this.initVodClient();
            GetMezzanineInfoRequest request = new GetMezzanineInfoRequest();
            request.setVideoId(videoId);
            request.setAuthTimeout(3600L);
            GetMezzanineInfoResponse response = acsClient.getAcsResponse(request);
            if (Objects.nonNull(response)) {
                return response.getMezzanine();
            }
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.GET_SOURCE_VIDEO_FAILED);
        } finally {
            this.closeAcsClient(acsClient);
        }
        return null;
    }

    @Override
    public Boolean removeVideo(List<String> videoIds) {
        DefaultAcsClient acsClient = null;
        try {
            acsClient = this.initVodClient();
            DeleteVideoRequest request = new DeleteVideoRequest();
            String ids = videoIds.stream().collect(Collectors.joining(","));
            request.setVideoIds(ids);
            DeleteVideoResponse response = acsClient.getAcsResponse(request);
            if (Objects.nonNull(response)) {
                return true;
            }
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.DELETE_VIDEO_FAILED);
        } finally {
            this.closeAcsClient(acsClient);
        }
        return false;
    }

    @Override
    public void auditVideoByAi(String videoId) {
        DefaultAcsClient acsClient = null;
        try {
            acsClient = this.initVodClient();
            SubmitAIMediaAuditJobRequest request = new SubmitAIMediaAuditJobRequest();
            request.setMediaId(videoId);
            acsClient.getAcsResponse(request);
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.SUBMIT_AI_AUDIT_FAILED);
        } finally {
            this.closeAcsClient(acsClient);
        }
    }

    @Override
    public void auditVideoByManual(String videoId) {
        DefaultAcsClient acsClient = null;
        try {
            acsClient = this.initVodClient();
            CreateAuditRequest request = new CreateAuditRequest();
            List<JSONObject> auditContents = new ArrayList<>(10);
            JSONObject auditContent = new JSONObject();
            auditContent.putOnce("VideoId", videoId);
            auditContent.putOnce("Status", "Normal");
            auditContents.add(auditContent);
            request.setAuditContent(auditContents.toString());
            acsClient.getAcsResponse(request);
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);

            throw new BizException(BizCodeEnum.SUBMIT_MANUAL_AUDIT_FAILED);
        } finally {
            this.closeAcsClient(acsClient);
        }
    }

    private UploadVideoVO getUploadVideoVO(byte[] data, String sourceFileName) throws ClientException {
        DefaultAcsClient acsClient = this.initVodClient();
        long fileSize = data.length;
        try {
            CreateUploadVideoRequest request = new CreateUploadVideoRequest();
            request.setTitle(FileUtil.getPrefix(sourceFileName));
            request.setFileName(sourceFileName);
            request.setFileSize(fileSize);
            CreateUploadVideoResponse acsResponse = acsClient.getAcsResponse(request);
            if (Objects.nonNull(acsResponse)) {
                String videoId = acsResponse.getVideoId();
                String uploadAuthStr = Base64.decodeStr(acsResponse.getUploadAuth());
                String uploadAddressStr = Base64.decodeStr(acsResponse.getUploadAddress());
                JsonNode uploadAuth = JacksonUtil.parseTree(uploadAuthStr);
                JsonNode uploadAddress = JacksonUtil.parseTree(uploadAddressStr);
                String objectName = uploadAddress.path("FileName").asText();

                String endpoint = uploadAddress.path("Endpoint").asText();
                String bucketName = uploadAddress.path("Bucket").asText();
                String accessKeyId = uploadAuth.path("AccessKeyId").asText();
                String accessKeySecret = uploadAuth.path("AccessKeySecret").asText();
                String securityToken = uploadAuth.path("SecurityToken").asText();

                // 使用本地 OSS 客户端上传，不修改共享 Bean 配置
                UploadVO result = uploadWithVodCredentials(data, sourceFileName, objectName,
                    endpoint, bucketName, accessKeyId, accessKeySecret, securityToken);
                if (Objects.nonNull(result)) {
                    return UploadVideoVO.builder().sourceFileName(result.getSourceFileName())
                        .fileName(result.getFileName())
                        .fileSize(result.getFileSize())
                        .relativePath(result.getRelativePath())
                        .videoId(videoId).build();
                }
            }
        } finally {
            this.closeAcsClient(acsClient);
        }
        return null;
    }

    private UploadVO uploadWithVodCredentials(byte[] data, String sourceFileName, String objectName,
        String endpoint, String bucketName, String accessKeyId, String accessKeySecret, String securityToken) {
        ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
        conf.setSupportCname(true);
        conf.setProtocol(Protocol.HTTPS);
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret, securityToken, conf);
        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(data));
            return UploadVO.builder()
                .sourceFileName(sourceFileName)
                .fileName(FileUtil.getName(objectName))
                .fileSize((long) data.length)
                .relativePath(objectName)
                .build();
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
            throw new BizException(BizCodeEnum.FILE_UPLOAD_ERROR);
        } finally {
            ossClient.shutdown();
        }
    }

    private DefaultAcsClient initVodClient() {
        final VodProperty.AliyunConfig aliyun = super.getConfig().getAliyun();
        AkSkDTO akSk = new AkSkDTO(aliyun.getAccessKey(), aliyun.getSecretKey());
        return super.initAcsClient(aliyun.getRegion(), akSk, aliyun.getSts());
    }

}
