/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 文件基础数据传输对象.
 *
 * @author zengdegui
 * @since 2026/9/6
 */
@Data
@SuperBuilder
@AllArgsConstructor
public class FileBaseDTO implements Serializable {

    /**
     * 文件名.
     */
    @Schema(description = "文件名")
    private String fileName;

    /**
     * 文件路径.
     */
    @Schema(description = "文件路径")
    private String filePath;
}
