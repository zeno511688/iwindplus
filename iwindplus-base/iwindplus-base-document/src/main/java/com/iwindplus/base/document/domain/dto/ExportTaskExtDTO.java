/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.document.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 导出任务扩展配置.
 *
 * @author zengdegui
 * @since 2025/1/1
 */
@Schema(description = "导出任务扩展配置")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTaskExtDTO implements Serializable {

    /**
     * 最大重试次数（可选）.
     */
    @Schema(description = "最大重试次数")
    private Integer maxAttempts;

    /**
     * 是否启用无限重试.
     */
    @Schema(description = "是否启用无限重试")
    private Boolean enabledUnlimitedRetry;

    /**
     * 扩展字段（用于存储其他自定义配置）.
     */
    @Schema(description = "扩展字段")
    private Map<String, Object> extra;
}
