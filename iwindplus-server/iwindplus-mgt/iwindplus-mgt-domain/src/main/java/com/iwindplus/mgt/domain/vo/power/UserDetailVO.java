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
 * 用户详情信息视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "用户详情信息视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailVO extends UserInfoVO {

    /**
     * 账号是否启用.
     */
    @Schema(description = "账号是否启用")
    private Boolean enabled;

    /**
     * 账号是否锁定.
     */
    @Schema(description = "账号是否锁定")
    private Boolean locked;

    /**
     * 账号是否过期.
     */
    @Schema(description = "账号是否过期")
    private Boolean accountExpired;

    /**
     * 凭证（密码）是否过期.
     */
    @Schema(description = "凭证（密码）是否过期")
    private Boolean credentialsExpired;

    /**
     * 密码.
     */
    @Schema(description = "密码")
    private String password;

    /**
     * 权限集合.
     */
    @Schema(description = "权限集合")
    private Set<String> permissions;
}
