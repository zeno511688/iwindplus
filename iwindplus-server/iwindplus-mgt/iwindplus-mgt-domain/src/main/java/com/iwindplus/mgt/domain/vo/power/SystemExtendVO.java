/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.power;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 资源扩展视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "资源扩展视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SystemExtendVO extends SystemVO {

    /**
     * 图标（绝对路径）.
     */
    @Schema(description = "图标（绝对路径）")
    private String iconUrlStr;
}