/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 对象存储预上传（预签名直传）数据传输对象.
 *
 * @author zengdegui
 * @since 2026/9/8
 */
@Schema(description = "对象存储预上传数据传输对象")
@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OssPreUploadDTO implements Serializable {

    /**
     * 配置编码（必填）.
     */
    @Schema(description = "配置编码")
    private String code;

    /**
     * 模板编码（必填）.
     */
    @Schema(description = "模板编码")
    private String tplCode;

    /**
     * 相对路径（必填，含文件名）.
     */
    @Schema(description = "相对路径")
    @NotBlank(message = "{relativePath.notEmpty}")
    private String relativePath;

    /**
     * 内容类型（可选，如：image/png）.
     */
    @Schema(description = "内容类型")
    private String contentType;

    /**
     * 过期时间（可选，单位：分钟，默认：60）.
     */
    @Schema(description = "过期时间（分钟）")
    private Integer timeout;

    /**
     * 是否重命名（可选）.
     */
    @Schema(description = "是否重命名")
    private Boolean renamed;
}
