/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发起流程实例返回视图对象.
 *
 * @author zengdegui
 * @since 2026/05/20 23:42
 */
@Schema(description = "发起流程实例返回视图对象")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowStartInstanceVO implements Serializable {

    /**
     * 实例主键.
     */
    @Schema(description = "实例主键")
    private Long id;

    /**
     * 实例单号.
     */
    @Schema(description = "实例单号")
    private String bizNumber;
}
