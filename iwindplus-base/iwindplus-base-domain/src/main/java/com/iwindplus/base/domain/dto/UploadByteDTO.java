/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 对象存储文件上传数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "对象存储文件上传数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UploadByteDTO implements Serializable {

    /**
     * 文件二进制流.
     */
    @Schema(description = "文件二进制流")
    @NotEmpty(message = "{data.notEmpty}")
    private Byte[] data;

    /**
     * 源文件名（必填）.
     */
    @Schema(description = "源文件名")
    @NotBlank(message = "{sourceFileName.notEmpty}")
    private String sourceFileName;

    /**
     * 内容类型（可选）.
     */
    @Schema(description = "内容类型")
    private String contentType;
}
