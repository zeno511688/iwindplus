/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.support;

import com.iwindplus.base.domain.enums.OssTypeEnum;
import com.iwindplus.base.domain.vo.FilePathVO;
import com.iwindplus.base.domain.vo.PreUploadVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.oss.domain.dto.OssCloudDownloadDTO;
import com.iwindplus.base.oss.domain.dto.OssCloudGetSignUrlDTO;
import com.iwindplus.base.oss.domain.dto.OssCloudListSignUrlDTO;
import com.iwindplus.base.oss.domain.dto.OssCloudPreUploadDTO;
import com.iwindplus.base.oss.domain.dto.OssCloudRemoveDTO;
import com.iwindplus.base.oss.domain.dto.OssCloudUploadDTO;
import com.iwindplus.base.oss.service.BaseService;
import java.util.List;

/**
 * OSS策略标识接口.
 *
 * <p>策略模式：不同的OSS类型实现此接口。每个配置对应一个运行时策略实例。</p>
 *
 * @author zengdegui
 * @since 2026/9/6
 */
public interface OssExecuteHandler extends BaseService {

    /**
     * 获取提供商类型.
     *
     * @return 提供商枚举
     */
    OssTypeEnum getProvider();

    /**
     * 预上传（生成预签名直传地址/凭证）.
     *
     * <p>调用方获取预签名上传地址后直接向OSS上传文件，文件流不经过业务服务中转。</p>
     *
     * @param request 预上传请求参数（必填）
     * @return PreUploadVO
     */
    PreUploadVO preUpload(OssCloudPreUploadDTO request);

    /**
     * 文件上传.
     *
     * @param request 上传请求参数（必填）
     * @return UploadVO
     */
    UploadVO uploadFile(OssCloudUploadDTO request);

    /**
     * 批量删除上传的文件.
     *
     * @param request 删除请求参数（必填）
     * @return boolean
     */
    boolean removeFiles(OssCloudRemoveDTO request);

    /**
     * 文件下载.
     *
     * @param request 下载请求参数（必填）
     */
    void downloadFile(OssCloudDownloadDTO request);

    /**
     * 获取文件访问路径.
     *
     * @param request 获取访问路径请求参数（必填）
     * @return FilePathVO
     */
    FilePathVO getSignUrl(OssCloudGetSignUrlDTO request);

    /**
     * 批量获取文件访问路径.
     *
     * @param request 批量获取访问路径请求参数（必填）
     * @return List<FilePathVO>
     */
    List<FilePathVO> listSignUrl(OssCloudListSignUrlDTO request);
}
