/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.support;

import com.aliyuncs.vod.model.v20170321.GetMezzanineInfoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoInfoResponse;
import com.iwindplus.base.domain.enums.VodTypeEnum;
import com.iwindplus.base.domain.vo.UploadVideoVO;
import com.iwindplus.base.oss.service.BaseService;
import java.io.File;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * 视频点播处理器接口.
 *
 * <p>策略模式：不同的视频点播类型实现此接口</p>
 *
 * @author zengdegui
 * @since 2025/08/31
 */
public interface VodExecuteHandler extends BaseService {

    /**
     * 获取提供商类型.
     *
     * @return 提供商枚举
     */
    VodTypeEnum getProvider();

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
