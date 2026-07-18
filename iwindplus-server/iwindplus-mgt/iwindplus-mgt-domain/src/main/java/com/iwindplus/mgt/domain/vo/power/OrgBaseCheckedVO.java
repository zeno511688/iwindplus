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
 * 组织基础字段视图对象（标记选中）.
 *
 * @author zengdegui
 * @since 2021/9/23
 */
@Schema(description = "组织基础字段视图对象（标记选中）")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrgBaseCheckedVO extends OrgBaseVO {

    /**
     * 是否选中（false:否 true：是）.
     */
    @Schema(description = "是否选中（false:否 true：是）")
    private Boolean checked;

    /**
     * 用户组织关系主键.
     */
    @Schema(description = "用户组织关系主键")
    private Long userOrgId;
}
