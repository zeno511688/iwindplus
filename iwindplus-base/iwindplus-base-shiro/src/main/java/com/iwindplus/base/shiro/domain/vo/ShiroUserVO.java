/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iwindplus.base.domain.vo.UserBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * shiro用户信息视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "shiro用户信息视图对象")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ShiroUserVO extends UserBaseVO {

    /**
     * 密码.
     */
    @Schema(description = "密码")
    @JsonIgnore
    private String password;

    /**
     * 角色权限编码集合.
     */
    @Schema(description = "角色权限编码集合")
    private Set<String> rolePermissions;

    /**
     * 资源权限编码集合.
     */
    @Schema(description = "资源权限编码集合")
    private Set<String> resourcePermissions;
}
