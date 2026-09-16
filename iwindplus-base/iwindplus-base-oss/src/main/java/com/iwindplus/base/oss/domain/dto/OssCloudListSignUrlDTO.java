/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.domain.dto;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * OSS批量获取文件访问路径请求参数.
 *
 * @author zengdegui
 * @since 2026/9/6
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OssCloudListSignUrlDTO implements Serializable {

    /**
     * 相对路径集合（必填）.
     */
    private List<String> relativePaths;

    /**
     * 过期时间（可选，单位：分钟，默认：1）.
     */
    private Integer timeout;

    /**
     * 线程池.
     */
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 空间名（必填）.
     */
    private String bucketName;

    /**
     * 访问域名（可选，自定义域名）.
     */
    private String accessDomain;
}
