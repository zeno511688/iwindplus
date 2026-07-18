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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 国际化项目扩展视图对象.
 *
 * @author zengdegui
 * @since 2020/4/14
 */
@Schema(description = "国际化项目扩展视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class I18nProjectExtendVO extends I18nProjectVO {

    /**
     * 内容.
     */
    @Schema(description = "内容")
    private String content;
}
