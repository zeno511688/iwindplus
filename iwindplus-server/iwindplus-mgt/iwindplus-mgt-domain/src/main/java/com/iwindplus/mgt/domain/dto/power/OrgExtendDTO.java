/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.power;

import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 组织扩展数据传输对象（处理大字段）.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Schema(description = "组织扩展数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrgExtendDTO extends DbVersionBaseDTO {
    /**
     * 简介.
     */
    @Schema(description = "简介")
    private String intro;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    @NotNull(message = "{orgId.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private Long orgId;
}
