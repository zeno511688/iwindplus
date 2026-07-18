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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 对象存储文件上传结果视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "对象存储文件上传结果视图对象")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UploadVO extends UploadBaseVO {

    /**
     * 访问域名.
     */
    @Schema(description = "访问域名")
    private String accessDomain;

    /**
     * 绝对路径.
     */
    @Schema(description = "绝对路径")
    private String absolutePath;
}
