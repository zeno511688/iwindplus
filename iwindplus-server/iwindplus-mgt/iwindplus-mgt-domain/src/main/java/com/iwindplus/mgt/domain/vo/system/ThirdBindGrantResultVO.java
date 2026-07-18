/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 第三方绑定授权结果视图对象.
 *
 * @author zengdegui
 * @since 2021/9/23
 */
@Schema(description = "第三方绑定授权结果视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdBindGrantResultVO implements Serializable {
    /**
     * 编码.
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 是否绑定用户
     */
    @Schema(description = "是否绑定用户")
    private Boolean bindFlag;
}
