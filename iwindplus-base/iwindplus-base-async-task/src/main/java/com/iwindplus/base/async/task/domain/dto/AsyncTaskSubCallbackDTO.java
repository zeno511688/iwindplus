/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步任务子任务回调通知数据传输对象.
 *
 * @author zengdegui
 * @since 2026/08/12 00:00
 */
@Schema(description = "异步任务子任务回调通知数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class AsyncTaskSubCallbackDTO extends AsyncTaskCallbackBaseDTO {

}
