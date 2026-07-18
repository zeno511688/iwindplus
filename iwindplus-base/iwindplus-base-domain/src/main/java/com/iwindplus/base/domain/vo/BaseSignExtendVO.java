/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 基础签名扩展视图对象.
 *
 * @author zengdegui
 * @since 2020/4/14
 */
@Schema(description = "基础签名扩展视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseSignExtendVO extends BaseSignVO {

    /**
     * 应用.
     */
    @Schema(description = "应用")
    private String application;

}
