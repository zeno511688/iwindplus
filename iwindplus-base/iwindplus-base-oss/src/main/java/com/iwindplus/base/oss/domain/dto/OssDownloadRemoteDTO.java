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
 * OSS远程文件下载请求参数.
 *
 * @author zengdegui
 * @since 2026/9/6
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OssDownloadRemoteDTO implements Serializable {

    /**
     * 响应（必填）.
     */
    private HttpServletResponse response;

    /**
     * 绝对路径（必填）.
     */
    private String absolutePath;

    /**
     * 新文件名（可选）.
     */
    private String fileName;
}
