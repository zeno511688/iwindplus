/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.power;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户登录信息视图对象.
 *
 * @author zengdegui
 * @since 2021/9/23
 */
@Schema(description = "用户登录信息视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVO extends UserInfoVO {

    /**
     * 年龄.
     */
    @Schema(description = "年龄")
    private Integer age;

    /**
     * 角色权限集合.
     */
    @Schema(description = "角色权限集合")
    private Set<RoleBaseVO> rolePermissions;

    /**
     * 按钮权限集合.
     */
    @Schema(description = "按钮权限集合")
    private Set<ResourceBaseVO> buttonPermissions;
}
