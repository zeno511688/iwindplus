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
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.domain.vo.UploadVideoVO;
import com.iwindplus.base.oss.domain.constant.OssConstant;
import com.iwindplus.base.oss.domain.dto.StsTokenDTO;
import com.iwindplus.base.oss.domain.property.OssProperty;
import com.iwindplus.base.oss.domain.property.OssProperty.AliyunConfig;
import com.iwindplus.base.oss.domain.property.VodProperty;
import com.iwindplus.base.oss.service.OssAliyunService;
import com.iwindplus.base.oss.service.VodAliyunService;
import com.iwindplus.base.util.FilesUtil;
import com.iwindplus.base.util.JacksonUtil;
import jakarta.annotation.Resource;
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
            final LocalDateTime expiration = LocalDateTime.ofInstant(Instant.parse(uploadAuth.path("ExpireUTCTime").asText()),
                    ZoneOffset.UTC.normalized())
                .atZone(ZoneOffset.UTC.normalized()).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();

            // 1. 先用通用 STS 模型接参
            StsTokenDTO stsToken = StsTokenDTO.builder()
                .accessKey(uploadAuth.path("AccessKeyId").asText())
                .secretKey(uploadAuth.path("AccessKeySecret").asText())
                .securityToken(uploadAuth.path("SecurityToken").asText())
                .expiration(expiration)
                .build();

            // 2. 构造阿里云配置
            AliyunConfig aliyun = AliyunConfig.builder()
                .endpoint(uploadAddress.path("Endpoint").asText())
                .bucketName(uploadAddress.path("Bucket").asText())
                .sts(stsToken)
                .build();

            // 3. 组装总配置
            final OssProperty ossProperty = OssProperty.builder()
                .aliyun(aliyun)
                .build();
            this.ossAliyunService.setConfig(ossProperty);

            UploadVO result = this.ossAliyunService.uploadFile(data, objectName, sourceFileName, Boolean.FALSE);
            if (Objects.nonNull(result)) {
                return UploadVideoVO.builder().sourceFileName(result.getSourceFileName())
                    .fileName(result.getFileName())
                    .fileSize(result.getFileSize())
                    .relativePath(result.getRelativePath())
                    .videoId(videoId).build();
            }
        }
        return null;
    }

    private DefaultAcsClient initVodClient() {
        final VodProperty.AliyunConfig aliyun = super.getConfig().getAliyun();
        AkSkDTO akSk = new AkSkDTO(aliyun.getAccessKey(), aliyun.getSecretKey());
        return super.initAcsClient(aliyun.getRegion(), akSk, aliyun.getSts());
    }

}
