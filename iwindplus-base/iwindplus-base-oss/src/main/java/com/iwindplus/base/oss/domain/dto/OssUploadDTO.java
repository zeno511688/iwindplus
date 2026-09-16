/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.domain.dto;

import com.iwindplus.base.domain.dto.UploadFileDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

/**
 * OSS文件上传请求参数.
 *
 * @author zengdegui
 * @since 2026/9/6
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OssUploadDTO extends UploadFileDTO {

    /**
     * 文件（data、file、url 三选一）.
     */
    @Schema(description = "文件")
    private MultipartFile file;

    /**
     * 相对路径（必填）.
     */
    @Schema(description = "相对路径")
    private String relativePath;

    /**
     * 是否重命名（可选）.
     */
    @Schema(description = "是否重命名")
    private Boolean renamed;
}
