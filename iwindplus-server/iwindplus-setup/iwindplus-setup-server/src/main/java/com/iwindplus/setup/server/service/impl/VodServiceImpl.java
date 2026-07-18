/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.server.service.impl;

import com.aliyuncs.vod.model.v20170321.GetMezzanineInfoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoInfoResponse;
import com.iwindplus.base.domain.enums.VodTypeEnum;
import com.iwindplus.base.domain.vo.UploadVideoVO;
import com.iwindplus.base.oss.domain.dto.StsTokenDTO;
import com.iwindplus.base.oss.domain.property.VodProperty;
import com.iwindplus.base.oss.service.VodAliyunService;
import com.iwindplus.setup.domain.vo.VodConfigVO;
import com.iwindplus.setup.server.service.VodConfigService;
import com.iwindplus.setup.server.service.VodService;
import java.io.File;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 视频点播业务层接口实现类.
 *
 * @author zengdegui
 * @since 2022/1/14
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class VodServiceImpl implements VodService {

    private final VodAliyunService vodAliyunService;
    private final VodConfigService vodConfigService;

    @Override
    public UploadVideoVO uploadVideo(String code, MultipartFile file) {
        final VodConfigVO vodConfig = this.vodConfigService.getByCode(code);
        if (VodTypeEnum.ALIYUN.equals(vodConfig.getType())) {
            this.buildVodConfigAliyun(vodConfig);
            return this.vodAliyunService.uploadVideo(file);
        }
        return null;
    }

    @Override
    public UploadVideoVO uploadVideoTwo(String code, String absolutePath) {
        final VodConfigVO vodConfig = this.vodConfigService.getByCode(code);
        if (VodTypeEnum.ALIYUN.equals(vodConfig.getType())) {
            this.buildVodConfigAliyun(vodConfig);
            return this.vodAliyunService.uploadVideo(new File(absolutePath));
        }
        return null;
    }

    @Override
    public String getPlayAuth(String code, String videoId, Long timeout) {
        final VodConfigVO vodConfig = this.vodConfigService.getByCode(code);
        if (VodTypeEnum.ALIYUN.equals(vodConfig.getType())) {
            this.buildVodConfigAliyun(vodConfig);
            return this.vodAliyunService.getPlayAuth(videoId, timeout);
        }
        return null;
    }

    @Override
    public GetVideoInfoResponse.Video getVideoInfo(String code, String videoId) {
        final VodConfigVO vodConfig = this.vodConfigService.getByCode(code);
        if (VodTypeEnum.ALIYUN.equals(vodConfig.getType())) {
            this.buildVodConfigAliyun(vodConfig);
            return this.vodAliyunService.getVideoInfo(videoId);
        }
        return null;
    }

    @Override
    public GetMezzanineInfoResponse.Mezzanine getSourceVideoInfo(String code, String videoId) {
        final VodConfigVO vodConfig = this.vodConfigService.getByCode(code);
        if (VodTypeEnum.ALIYUN.equals(vodConfig.getType())) {
            this.buildVodConfigAliyun(vodConfig);
            return this.vodAliyunService.getSourceVideoInfo(videoId);
        }
        return null;
    }

    @Override
    public boolean removeVideo(String code, List<String> videoIds) {
        final VodConfigVO vodConfig = this.vodConfigService.getByCode(code);
        if (VodTypeEnum.ALIYUN.equals(vodConfig.getType())) {
            this.buildVodConfigAliyun(vodConfig);
            return this.vodAliyunService.removeVideo(videoIds);
        }
        return false;
    }

    private void buildVodConfigAliyun(VodConfigVO vodConfig) {
        final StsTokenDTO stsToken = StsTokenDTO.builder()
            .endpoint(vodConfig.getStsEndpoint())
            .roleArn(vodConfig.getRoleArn())
            .policy(vodConfig.getPolicy())
            .build();
        final VodProperty.AliyunConfig aliyunConfig = VodProperty.AliyunConfig.builder()
            .accessKey(vodConfig.getAccessKey())
            .secretKey(vodConfig.getSecretKey())
            .region(vodConfig.getRegion())
            .sts(stsToken)
            .build();
        VodProperty config = VodProperty.builder()
            .aliyun(aliyunConfig)
            .build();
        this.vodAliyunService.setConfig(config);
    }
}
