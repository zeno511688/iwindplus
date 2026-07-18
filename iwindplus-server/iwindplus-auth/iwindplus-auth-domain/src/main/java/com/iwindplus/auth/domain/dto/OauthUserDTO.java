/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 认证用户信息数据传输对象.
 *
 * @author zengdegui
 * @since 2020/4/3
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OauthUserDTO implements UserDetails, CredentialsContainer {

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
     * 用户主键.
     */
    @Schema(description = "用户主键")
    private Long userId;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;

    /**
     * 工号
     */
    @Schema(description = "工号")
    private String jobNumber;

    /**
     * 用户名.
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 密码.
     */
    @Schema(description = "密码")
    private String password;

    /**
     * 手机.
     */
    @Schema(description = "手机")
    private String mobile;

    /**
     * 姓名.
     */
    @Schema(description = "姓名")
    private String realName;

    /**
     * 邮箱.
     */
    @Schema(description = "邮箱")
    private String mail;

    /**
     * 身份证.
     */
    @Schema(description = "身份证")
    private String idCard;

    /**
     * 昵称.
     */
    @Schema(description = "昵称")
    private String nickName;

    /**
     * 权限列表.
     */
    @Schema(description = "权限列表")
    private Set<GrantedAuthority> authorities;

    @Override
    public boolean isAccountNonExpired() {
        return !Optional.ofNullable(this.accountExpired).orElse(Boolean.TRUE);
    }

    @Override
    public boolean isAccountNonLocked() {
        return !Optional.ofNullable(this.locked).orElse(Boolean.TRUE);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !Optional.ofNullable(this.credentialsExpired).orElse(Boolean.TRUE);
    }

    @Override
    public boolean isEnabled() {
        return Optional.ofNullable(this.enabled).orElse(Boolean.FALSE);
    }

    /**
     * 认证完成后删除凭证.
     */
    @Override
    public void eraseCredentials() {
        this.password = null;
    }

    /**
     * 添加授权.
     *
     * @param permissions 权限
     */
    public void addGrantedAuthority(Set<String> permissions) {
        Stream<GrantedAuthority> roleStream = permissions.stream().filter(Objects::nonNull).map(SimpleGrantedAuthority::new);
        this.authorities = roleStream.sorted(Comparator.comparing(GrantedAuthority::getAuthority))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
