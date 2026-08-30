/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步任务组任务视图对象.
 *
 * @author zengdegui
 * @since 2026/08/02 13:59
 */
@Schema(description = "异步任务组任务视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncTaskGroupVO extends AsyncTaskBaseVO {

    /**
     * 子任务集合.
     */
    @Schema(description = "子任务集合")
    private List<AsyncTaskSubBaseVO> subTasks;
}
