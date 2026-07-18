/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.domain.dto.power;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Set;

/**
 * 组织添加用户数据传输对象.
 *
 * @author zengdegui
 * @since 2019/8/23
 */
@Schema(description = "组织添加用户数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrgSaveUserDTO implements Serializable {

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    @NotNull(message = "{userId.notEmpty}")
    private Long userId;

    /**
     * 职位主键集合.
     */
    @Schema(description = "职位主键集合")
    @NotEmpty(message = "{positionIds.notEmpty}")
    private Set<Long> positionIds;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}
