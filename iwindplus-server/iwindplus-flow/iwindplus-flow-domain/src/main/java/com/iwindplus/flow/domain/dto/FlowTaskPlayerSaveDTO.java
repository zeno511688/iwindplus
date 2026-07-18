/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 流程任务参与人保存数据传输对象.
 *
 * @author zengdegui
 * @since 2026/01/11 20:14
 */
@Schema(description = "流程任务参与人保存数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class FlowTaskPlayerSaveDTO extends FlowTaskPlayerDTO {

}
