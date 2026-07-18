/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.dto;

import com.iwindplus.base.domain.vo.UserBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 发起流程实例数据传输对象.
 *
 * @author zengdegui
 * @since 2026/05/20 23:27
 */
@Schema(description = "发起流程实例数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowStartInstanceDTO implements Serializable {

    /**
     * 模型编码.
     */
    @Schema(description = "模型编码")
    @NotBlank(message = "{modelCode.notEmpty}")
    private String modelCode;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

    /**
     * 回调地址.
     */
    @Schema(description = "回调地址")
    @NotBlank(message = "{callbackUrl.notEmpty}")
    private String callbackUrl;

    /**
     * 流程变量.
     */
    @Schema(description = "流程变量")
    @NotEmpty(message = "{variables.notEmpty}")
    private Map<String, Object> variables;

    /**
     * 当前用户
     */
    @Schema(description = "当前用户")
    private UserBaseVO currentUser;
}
