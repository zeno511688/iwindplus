/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.domain.dto;

import jakarta.servlet.http.HttpServletResponse;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * OSS文件下载请求参数基类.
 *
 * <p>本地文件下载与云OSS下载的公共参数，不含云OSS特有字段。</p>
 *
 * @author zengdegui
 * @since 2026/9/6
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OssDownloadDTO implements Serializable {

    /**
     * 响应（必填）.
     */
    private HttpServletResponse response;

    /**
     * 相对路径（必填）.
     */
    private String relativePath;

    /**
     * 新文件名（必填）.
     */
    private String fileName;
}
