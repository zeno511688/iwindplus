/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 对象存储上传数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "对象存储上传数据传输对象")
@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OssUploadFileDTO implements Serializable {

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
     * 字节数组（data、url 二选一）.
     */
    @Schema(description = "字节数组")
    private byte[] data;

    /**
     * 外网文件地址（data、url 二选一）.
     */
    @Schema(description = "外网文件地址")
    private String url;

    /**
     * 相对路径（必填）.
     */
    @Schema(description = "相对路径")
    @NotEmpty(message = "{relativePath.notEmpty}")
    private String relativePath;

    /**
     * 源文件名（必填）.
     */
    @Schema(description = "源文件名")
    private String sourceFileName;

    /**
     * 内容类型（可选）.
     */
    @Schema(description = "内容类型")
    private String contentType;

    /**
     * 是否重命名（可选）.
     */
    @Schema(description = "是否重命名")
    private Boolean renamed;
}
