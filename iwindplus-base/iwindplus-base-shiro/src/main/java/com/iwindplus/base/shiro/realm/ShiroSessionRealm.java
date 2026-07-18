/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro.realm;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.shiro.domain.dto.ShiroSessionTokenDTO;
import com.iwindplus.base.shiro.domain.vo.ShiroUserVO;
import com.iwindplus.base.shiro.exception.BizShiroAuthenticationException;
import com.iwindplus.base.shiro.service.ShiroService;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * 有状态方式relam（用户密码方式）.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ShiroSessionRealm extends AuthorizingRealm {

    private ShiroService shiroService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof ShiroSessionTokenDTO;
    }

    /**
     * 认证.
     *
     * @param authenticationToken 认证token
     * @return AuthenticationInfo
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
        throws AuthenticationException {
        ShiroSessionTokenDTO param = (ShiroSessionTokenDTO) authenticationToken;
        String username = param.getUsername();
        ShiroUserVO data = this.shiroService.getByUsername(username);
        if (Objects.isNull(data)) {
            throw new BizShiroAuthenticationException(BizCodeEnum.ACCOUNT_NOT_EXIST);
        }
        return new SimpleAuthenticationInfo(data, data.getPassword(), this.getName());
    }

    /**
     * 鉴权.
     *
     * @param principals 用户信息
     * @return AuthorizationInfo
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        ShiroUserVO data = (ShiroUserVO) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        // 角色权限
        Set<String> rolePerms = data.getRolePermissions();
        if (CollUtil.isNotEmpty(rolePerms)) {
            info.setRoles(rolePerms);
        }
        // 资源权限
        Set<String> resourcePerms = data.getResourcePermissions();
        if (CollUtil.isNotEmpty(resourcePerms)) {
            info.setStringPermissions(resourcePerms);
        }
        return info;
    }

    /**
     * 检验密码.
     *
     * @param token token
     * @param info  认证信息
     * @throws AuthenticationException
     */
    @Override
    protected void assertCredentialsMatch(AuthenticationToken token, AuthenticationInfo info)
        throws AuthenticationException {
        CredentialsMatcher cm = getCredentialsMatcher();
        if (!cm.doCredentialsMatch(token, info)) {
            throw new BizShiroAuthenticationException(BizCodeEnum.PASSWORD_ERROR);
        }
    }

    /**
     * 重写方法,清除当前用户的授权缓存.
     *
     * @param principals
     */
    @Override
    public void clearCachedAuthorizationInfo(PrincipalCollection principals) {
        super.clearCachedAuthorizationInfo(principals);
    }

    /**
     * 重写方法，清除当前用户的认证缓存.
     *
     * @param principals
     */
    @Override
    public void clearCachedAuthenticationInfo(PrincipalCollection principals) {
        super.clearCachedAuthenticationInfo(principals);
    }

    @Override
    public void clearCache(PrincipalCollection principals) {
        super.clearCache(principals);
    }

    /**
     * 自定义方法：清除所有授权缓存.
     */
    public void clearAllCachedAuthorizationInfo() {
        getAuthorizationCache().clear();
    }

    /**
     * 自定义方法：清除所有认证缓存.
     */
    public void clearAllCachedAuthenticationInfo() {
        getAuthenticationCache().clear();
    }

    /**
     * 自定义方法：清除所有的认证缓存和授权缓存.
     */
    public void clearAllCache() {
        clearAllCachedAuthenticationInfo();
        clearAllCachedAuthorizationInfo();
    }
}
