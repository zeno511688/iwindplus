/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 对象存储预上传（预签名直传）结果视图对象.
 *
 * @author zengdegui
 * @since 2026/9/8
 */
@Schema(description = "对象存储预上传结果视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PreUploadVO implements Serializable {

    /**
     * 相对路径.
     */
    @Schema(description = "相对路径")
    private String relativePath;

    /**
     * 预签名上传地址/凭证（阿里云/MinIO为预签名URL，七牛云为上传凭证）.
     */
    @Schema(description = "预签名上传地址/凭证")
    private String uploadUrl;

    /**
     * 过期时间（单位：分钟）.
     */
    @Schema(description = "过期时间（分钟）")
    private Integer timeout;
}
