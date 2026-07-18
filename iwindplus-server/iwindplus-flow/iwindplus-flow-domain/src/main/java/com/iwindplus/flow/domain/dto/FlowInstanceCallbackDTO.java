/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.dto;

import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.flow.domain.enums.FlowInstanceCallbackStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 流程实例回调数据传输对象.
 *
 * @author zengdegui
 * @since 2026/01/11 20:14
 */
@Schema(description = "流程实例回调数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowInstanceCallbackDTO extends DbVersionBaseDTO {

    /**
     * 状态.
     */
    @Schema(description = "状态")
    private FlowInstanceCallbackStatusEnum status;

    /**
     * 分类名称.
     */
    @Schema(description = "分类名称")
    private String categoryName;

    /**
     * 模型名称.
     */
    @Schema(description = "模型名称")
    private String modelName;

    /**
     * 实例名称.
     */
    @Schema(description = "实例名称")
    private String instanceName;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

    /**
     * 回调地址.
     */
    @Schema(description = "回调地址")
    private String callbackUrl;

    /**
     * 变量.
     */
    @Schema(description = "变量")
    private String variable;

    /**
     * 重试次数.
     */
    @Schema(description = "重试次数")
    private Integer retryCount;

    /**
     * 分类主键.
     */
    @Schema(description = "分类主键")
    @NotNull(message = "{categoryId.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private Long categoryId;

    /**
     * 模型主键.
     */
    @Schema(description = "模型主键")
    @NotNull(message = "{modelId.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private Long modelId;

    /**
     * 实例主键.
     */
    @Schema(description = "实例主键")
    @NotNull(message = "{instanceId.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private Long instanceId;
}
