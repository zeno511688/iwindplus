/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.application.service;

import com.aliyuncs.vod.model.v20170321.GetMezzanineInfoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoInfoResponse;
import com.iwindplus.base.domain.enums.VodTypeEnum;
import com.iwindplus.base.domain.vo.UploadVideoVO;
import com.iwindplus.base.oss.factory.VodExecuteHandlerFactory;
import com.iwindplus.base.oss.support.VodExecuteHandler;
import java.io.File;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 视频点播业务层接口类.
 *
 * @author zengdegui
 * @since 2022/1/14
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class VodApplicationService {

    private final VodExecuteHandlerFactory vodExecuteHandlerFactory;

    /**
     * 视频文件上传
     *
     * @param type 视频点播类型（必填）
     * @param code 配置编码（必填）
     * @param file 文件（必填）
     * @return UploadVideoVO
     */
    public UploadVideoVO uploadVideo(VodTypeEnum type, String code, MultipartFile file) {
        if (VodTypeEnum.ALIYUN.equals(type)) {
            final VodExecuteHandler handler = this.vodExecuteHandlerFactory.getHandler(type, code);
            return handler.uploadVideo(file);
        }
        return null;
    }

    /**
     * 视频文件上传
     *
     * @param type         视频点播类型（必填）
     * @param code         配置编码（必填）
     * @param absolutePath 绝对路径（必填）
     * @return UploadVideoVO
     */
    public UploadVideoVO uploadVideoTwo(VodTypeEnum type, String code, String absolutePath) {
        if (VodTypeEnum.ALIYUN.equals(type)) {
            final VodExecuteHandler handler = this.vodExecuteHandlerFactory.getHandler(type, code);
            return handler.uploadVideo(new File(absolutePath));
        }
        return null;
    }

    /**
     * 获取播放凭证.
     *
     * @param type    视频点播类型（必填）
     * @param code    配置编码（必填）
     * @param videoId 视频标识（必填）
     * @param timeout 过期时间（单位：分钟，默认：30）
     * @return String
     */
    public String getPlayAuth(VodTypeEnum type, String code, String videoId, Long timeout) {
        if (VodTypeEnum.ALIYUN.equals(type)) {
            final VodExecuteHandler handler = this.vodExecuteHandlerFactory.getHandler(type, code);
            return handler.getPlayAuth(videoId, timeout);
        }
        return null;
    }

    /**
     * 获取视频信息.
     *
     * @param type    视频点播类型（必填）
     * @param code    配置编码（必填）
     * @param videoId 视频标识（必填）
     * @return GetVideoInfoResponse.Video
     */
    public GetVideoInfoResponse.Video getVideoInfo(VodTypeEnum type, String code, String videoId) {
        if (VodTypeEnum.ALIYUN.equals(type)) {
            final VodExecuteHandler handler = this.vodExecuteHandlerFactory.getHandler(type, code);
            return handler.getVideoInfo(videoId);
        }
        return null;
    }

    /**
     * 获取源视频信息.
     *
     * @param type    视频点播类型（必填）
     * @param code    配置编码（必填）
     * @param videoId 视频标识（必填）
     * @return GetMezzanineInfoResponse.Mezzanine
     */
    public GetMezzanineInfoResponse.Mezzanine getSourceVideoInfo(VodTypeEnum type, String code, String videoId) {
        if (VodTypeEnum.ALIYUN.equals(type)) {
            final VodExecuteHandler handler = this.vodExecuteHandlerFactory.getHandler(type, code);
            return handler.getSourceVideoInfo(videoId);
        }
        return null;
    }

    /**
     * 删除视频.
     *
     * @param type     视频点播类型（必填）
     * @param code     配置编码（必填）
     * @param videoIds 视频标识集合（必填）
     * @return boolean
     */
    public boolean removeVideo(VodTypeEnum type, String code, List<String> videoIds) {
        if (VodTypeEnum.ALIYUN.equals(type)) {
            final VodExecuteHandler handler = this.vodExecuteHandlerFactory.getHandler(type, code);
            return handler.removeVideo(videoIds);
        }
        return false;
    }

}
