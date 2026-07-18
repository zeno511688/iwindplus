/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.power;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户组保存/修改数据传输对象.
 *
 * @author zengdegui
 * @since 2019/8/23
 */
@Schema(description = "用户组保存/修改数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupSaveEditDTO extends UserGroupDTO {

    /**
     * 当前登录用户主键.
     */
    @Schema(description = "当前登录用户主键")
    private Long currentUserId;

    /**
     * 角色主键集合.
     */
    @Schema(description = "角色主键集合")
    private Set<Long> roleIds;
}
