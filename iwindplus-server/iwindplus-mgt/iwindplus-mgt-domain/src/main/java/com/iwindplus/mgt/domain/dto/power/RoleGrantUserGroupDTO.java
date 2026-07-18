/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.domain.dto.power;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * 角色授权用户组数据传输对象.
 *
 * @author zengdegui
 * @since 2021/5/5
 */
@Schema(description = "角色授权用户组数据传输对象")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleGrantUserGroupDTO implements Serializable {
    /**
     * 角色主键.
     */
    @Schema(description = "角色主键")
    @NotNull(message = "{roleId.notEmpty}")
    private Long roleId;

    /**
     * 用户主键集合.
     */
    @Schema(description = "用户组主键集合")
    private Set<Long> userGroupIds;
}
