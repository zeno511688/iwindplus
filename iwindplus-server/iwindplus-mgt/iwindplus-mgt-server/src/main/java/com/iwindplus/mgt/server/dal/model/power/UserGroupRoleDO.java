/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.model.power;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iwindplus.base.mybatis.domain.DbBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户组角色关系表.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "用户组角色关系对象")
@TableName(value = "`user_group_role`")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupRoleDO extends DbBaseDO {

    /**
     * 用户组主键.
     */
    @Schema(description = "用户组主键")
    private Long userGroupId;

    /**
     * 角色主键.
     */
    @Schema(description = "角色主键")
    private Long roleId;
}