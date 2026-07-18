/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 对象存储文件上传基础结果视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "对象存储文件上传基础结果视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UploadBaseVO implements Serializable {

    /**
     * 原始文件名.
     */
    @Schema(description = "原始文件名")
    private String sourceFileName;

    /**
     * 新文件名.
     */
    @Schema(description = "新文件名")
    private String fileName;

    /**
     * 文件大小.
     */
    @Schema(description = "文件大小")
    private Long fileSize;

    /**
     * 相对路径.
     */
    @Schema(description = "相对路径")
    private String relativePath;
}
