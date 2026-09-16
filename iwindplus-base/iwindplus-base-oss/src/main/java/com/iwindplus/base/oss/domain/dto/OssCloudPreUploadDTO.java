/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 云OSS预上传（预签名直传）请求参数.
 *
 * <p>用于生成预签名上传地址/凭证，调用方直接向OSS上传文件，避免文件流经过业务服务中转。</p>
 *
 * @author zengdegui
 * @since 2026/9/8
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OssCloudPreUploadDTO implements Serializable {

    /**
     * 空间名（必填）.
     */
    private String bucketName;

    /**
     * 访问域名（可选，自定义域名）.
     */
    private String accessDomain;

    /**
     * 相对路径（必填，含文件名）.
     */
    private String relativePath;

    /**
     * 内容类型（可选，如：image/png）.
     */
    private String contentType;

    /**
     * 过期时间（可选，单位：分钟，默认：1）.
     */
    private Integer timeout;

    /**
     * 是否重命名（可选）.
     */
    @Schema(description = "是否重命名")
    private Boolean renamed;
}
