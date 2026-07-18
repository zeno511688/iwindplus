/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.power;

import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户组织关系数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "用户组织关系数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserOrgDTO extends DbVersionBaseDTO {

    /**
     * 是否选中（用于切换组织，false:否 true：是）.
     */
    @Schema(description = "是否选中（用于切换组织，false:否 true：是）")
    private Boolean checked;

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    private Long userId;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}