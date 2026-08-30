/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.domain.dto;

import com.iwindplus.base.async.task.support.AsyncTaskSubHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步任务子任务提交数据传输对象.
 *
 * @author zengdegui
 * @since 2025/12/28 00:22
 */
@Schema(description = "异步任务子任务提交数据传输对象")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AsyncTaskSubSubmitDTO extends AsyncTaskSubmitBaseDTO {

    /**
     * 执行器类（必填）.
     */
    @Schema(description = "执行器类")
    private Class<? extends AsyncTaskSubHandler> executorClass;

    /**
     * 排序号（必填）
     */
    @Schema(description = "排序号")
    private Integer seq;

    /**
     * 阶段（同阶段子任务并发）.
     */
    @Schema(description = "阶段")
    private Integer stage;

    /**
     * 扩展对象.
     */
    @Schema(description = "扩展对象")
    private AsyncTaskSubExtDTO ext;
}
