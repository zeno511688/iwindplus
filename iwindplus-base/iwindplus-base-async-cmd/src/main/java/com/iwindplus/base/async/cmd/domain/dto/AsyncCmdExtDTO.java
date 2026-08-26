/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步命令扩展配置.
 *
 * @author zengdegui
 * @since 2025/1/1
 */
@Schema(description = "异步命令扩展配置")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdExtDTO {

    /**
     * 成功后是否删除.
     */
    @Schema(description = "成功后是否删除")
    private Boolean enabledSuccessDelete;
}
