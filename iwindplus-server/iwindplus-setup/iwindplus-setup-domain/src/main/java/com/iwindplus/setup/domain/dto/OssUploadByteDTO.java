/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.domain.dto;

import com.iwindplus.base.domain.dto.UploadByteDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 对象存储上传（字节数组）数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "对象存储上传（字节数组）数据传输对象")
@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OssUploadByteDTO implements Serializable {

    /**
     * 请求唯一标识.
     */
    @Schema(description = "请求唯一标识")
    @Length(max = 100, message = "{requestId.length}")
    private String requestId;

    /**
     * 模板编码（必填）.
     */
    @Schema(description = "模板编码")
    @NotBlank(message = "{tplCode.notEmpty}")
    private String tplCode;

    /**
     * 存储目录前缀（必填）.
     */
    @Schema(description = "存储目录前缀")
    @NotBlank(message = "{prefix.notEmpty}")
    private String prefix;

    /**
     * 是否重命名文件名（可选，默认：true）.
     */
    @Schema(description = "是否重命名文件名")
    private Boolean renamed;

    /**
     * 附件.
     */
    @Schema(description = "附件")
    private UploadByteDTO attachment;
}
