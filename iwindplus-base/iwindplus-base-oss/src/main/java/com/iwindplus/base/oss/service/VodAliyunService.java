/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.service;

import com.aliyuncs.vod.model.v20170321.GetMezzanineInfoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoInfoResponse;
import com.iwindplus.base.domain.vo.UploadVideoVO;
import com.iwindplus.base.oss.domain.property.VodProperty;
import java.io.File;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * 阿里云视频点播业务层接口类.
 *
 * @author zengdegui
 * @since 2022/1/14
 */
public interface VodAliyunService extends VodBaseService, BaseConfigService<VodProperty> {

    /**
     * 视频文件上传.
     *
     * @param file 文件（必填）
     * @return UploadVideoVO
     */
    UploadVideoVO uploadVideo(MultipartFile file);

    /**
     * 视频文件上传.
     *
     * @param file 文件（必填）
     * @return UploadVideoVO
     */
    UploadVideoVO uploadVideo(File file);

    /**
     * 获取播放凭证.
     *
     * @param videoId 视频标识（必填）
     * @param timeout 过期时间（可选，单位：分钟，默认：60）
     * @return String
     */
    String getPlayAuth(String videoId, Long timeout);

    /**
     * 获取视频信息.
     *
     * @param videoId 视频标识（必填）
     * @return GetVideoInfoResponse.Video
     */
    GetVideoInfoResponse.Video getVideoInfo(String videoId);

    /**
     * 获取源视频信息.
     *
     * @param videoId 视频标识（必填）
     * @return GetMezzanineInfoResponse.Mezzanine
     */
    GetMezzanineInfoResponse.Mezzanine getSourceVideoInfo(String videoId);

    /**
     * 删除视频.
     *
     * @param videoIds 视频标识集合（必填）
     * @return Boolean
     */
    Boolean removeVideo(List<String> videoIds);

    /**
     * 提交智能AI审核作业.
     *
     * @param videoId 视频标识（必填）
     */
    void auditVideoByAi(String videoId);

    /**
     * 提交人工审核作业.
     *
     * @param videoId 视频标识（必填）
     */
    void auditVideoByManual(String videoId);
}
