/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 云OSS文件上传请求参数.
 *
 * <p>在云OSS基类之上，增加上传特有字段（字节数组、文件、相对路径、源文件名）。</p>
 *
 * @author zengdegui
 * @since 2026/9/6
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OssCloudUploadDTO extends OssUploadDTO {

    /**
     * 空间名（必填）.
     */
    private String bucketName;

    /**
     * 访问域名（可选，自定义域名）.
     */
    private String accessDomain;

    /**
     * 是否返回绝对路径（可选，默认：true）.
     */
    private Boolean returnAbsolutePath;
}
