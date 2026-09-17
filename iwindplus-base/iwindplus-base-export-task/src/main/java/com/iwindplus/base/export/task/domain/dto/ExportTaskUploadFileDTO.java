/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 导出任务上传文件数据传输对象.
 *
 * @author zengdegui
 * @since 2026/09/17 16:18
 */
@Schema(description = "导出任务上传文件数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTaskUploadFileDTO implements Serializable {

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
     * 字节数组.
     */
    @Schema(description = "字节数组")
    private byte[] data;

    /**
     * 相对路径（必填）.
     */
    @Schema(description = "相对路径")
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
