/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.dto;

import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.OtherEditGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 数据库基础通用字段数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "数据库基础通用字段数据传输对象")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DbBaseDTO extends DbCommonDTO {

    /**
     * 主键.
     */
    @Schema(description = "主键")
    @NotNull(message = "{id.notEmpty}", groups = {EditGroup.class, OtherEditGroup.class})
    private Long id;
}
