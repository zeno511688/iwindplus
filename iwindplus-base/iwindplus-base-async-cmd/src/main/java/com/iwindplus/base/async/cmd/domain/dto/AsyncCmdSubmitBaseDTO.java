/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步命令提交基础数据传输对象.
 *
 * @author zengdegui
 * @since 2025/12/28 00:22
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdSubmitBaseDTO implements Serializable {

    /**
     * 业务key（必填），例如 ORDER.
     */
    @Schema(description = "业务key，例如 ORDER")
    private String bizKey;

    /**
     * 业务类型（必填），例如 ORDER、USER.
     */
    @Schema(description = "业务类型，例如 ORDER_CREATE")
    private String bizType;
}
