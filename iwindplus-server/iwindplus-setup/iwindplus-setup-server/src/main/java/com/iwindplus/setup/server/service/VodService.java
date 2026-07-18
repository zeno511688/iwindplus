/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.server.service;

import com.aliyuncs.vod.model.v20170321.GetMezzanineInfoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoInfoResponse;
import com.iwindplus.base.domain.vo.UploadVideoVO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * 视频点播业务层接口类.
 *
 * @author zengdegui
 * @since 2022/1/14
 */
public interface VodService {

    /**
     * 视频文件上传
     *
     * @param code 配置编码（必填）
     * @param file 文件（必填）
     * @return UploadVideoVO
     */
    UploadVideoVO uploadVideo(String code, MultipartFile file);

    /**
     * 视频文件上传
     *
     * @param code         配置编码（必填）
     * @param absolutePath 绝对路径（必填）
     * @return UploadVideoVO
     */
    UploadVideoVO uploadVideoTwo(String code, String absolutePath);

    /**
     * 获取播放凭证.
     *
     * @param code    配置编码（必填）
     * @param videoId 视频标识（必填）
     * @param timeout 过期时间（单位：分钟，默认：30）
     * @return String
     */
    String getPlayAuth(String code, String videoId, Long timeout);

    /**
     * 获取视频信息.
     *
     * @param code    配置编码（必填）
     * @param videoId 视频标识（必填）
     * @return GetVideoInfoResponse.Video
     */
    GetVideoInfoResponse.Video getVideoInfo(String code, String videoId);

    /**
     * 获取源视频信息.
     *
     * @param code    配置编码（必填）
     * @param videoId 视频标识（必填）
     * @return GetMezzanineInfoResponse.Mezzanine
     */
    GetMezzanineInfoResponse.Mezzanine getSourceVideoInfo(String code, String videoId);

    /**
     * 删除视频.
     *
     * @param code     配置编码（必填）
     * @param videoIds 视频标识集合（必填）
     * @return boolean
     */
    boolean removeVideo(String code, List<String> videoIds);
}
