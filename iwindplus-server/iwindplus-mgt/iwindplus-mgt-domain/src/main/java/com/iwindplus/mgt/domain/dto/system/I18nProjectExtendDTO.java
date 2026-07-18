/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 国际化项目扩展数据传输对象.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Schema(description = "国际化项目扩展数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class I18nProjectExtendDTO extends I18nProjectDTO {

    /**
     * 内容.
     */
    @Schema(description = "内容")
    private String content;
}
