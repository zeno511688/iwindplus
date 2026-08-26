/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步命令子任务扩展配置.
 *
 * @author zengdegui
 * @since 2025/1/1
 */
@Schema(description = "异步命令子任务扩展配置")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdSubExtDTO implements Serializable {

    /**
     * 扩展字段（用于存储其他自定义配置）.
     */
    @Schema(description = "扩展字段")
    private Map<String, Object> extra;
}
