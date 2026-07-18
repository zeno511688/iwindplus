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
 * 文件路径视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "文件路径视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FilePathVO implements Serializable {

    /**
     * 访问域名.
     */
    @Schema(description = "访问域名")
    private String accessDomain;

    /**
     * 相对路径.
     */
    @Schema(description = "相对路径")
    private String relativePath;

    /**
     * 绝对路径.
     */
    @Schema(description = "绝对路径")
    private String absolutePath;
}
