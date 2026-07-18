/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro.realm;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.shiro.domain.constant.ShiroConstant;
import com.iwindplus.base.shiro.domain.dto.ShiroTokenDTO;
import com.iwindplus.base.shiro.domain.property.ShiroProperty;
import com.iwindplus.base.shiro.domain.vo.AccessTokenVO;
import com.iwindplus.base.shiro.domain.vo.ShiroUserVO;
import com.iwindplus.base.shiro.exception.BizShiroAuthenticationException;
import com.iwindplus.base.shiro.service.ShiroService;
import java.math.BigDecimal;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 无状态方式relam.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ShiroRealm extends AuthorizingRealm {

    private ShiroService shiroService;

    private RedisTemplate<String, Object> redisTemplate;

    private ShiroProperty shiroProperty;

    private PasswordEncoder passwordEncoder;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof ShiroTokenDTO;
    }

    /**
     * 认证.
     *
     * @param authenticationToken 认证token
     * @return AuthenticationInfo
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        ShiroTokenDTO param = (ShiroTokenDTO) authenticationToken;
        String accessToken = param.getAccessToken();
        String keyPrefix = getKeyPrefix(ShiroConstant.ACCESS_TOKEN_PREFIX);
        String accessTokenKey = new StringBuilder(keyPrefix).append(accessToken).toString();
        Object obj = this.redisTemplate.opsForValue().get(accessTokenKey);
        if (Objects.isNull(obj)) {
            throw new BizShiroAuthenticationException(BizCodeEnum.INVALID_ACCESS_TOKEN);
        }
        String username = obj.toString();
        ShiroUserVO data = this.shiroService.getByUsername(username);
        if (Objects.isNull(data)) {
            throw new BizShiroAuthenticationException(BizCodeEnum.ACCOUNT_NOT_EXIST);
        }
        return new SimpleAuthenticationInfo(data, accessToken, this.getName());
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
     * 检验访问token.
     *
     * @param token token
     * @param info  认证信息
     * @throws AuthenticationException
     */
    @Override
    protected void assertCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) throws AuthenticationException {
        CredentialsMatcher cm = getCredentialsMatcher();
        if (!cm.doCredentialsMatch(token, info)) {
            throw new BizShiroAuthenticationException(BizCodeEnum.INVALID_ACCESS_TOKEN);
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

    /**
     * 通过用户名密码，生成访问token和刷新token.
     *
     * @param username 用户名
     * @param password 密码
     * @return AccessTokenVO
     */
    public AccessTokenVO getAccessTokenByUsername(String username, String password) {
        ShiroUserVO data = this.shiroService.getByUsername(username);
        if (Objects.isNull(data)) {
            throw new BizException(BizCodeEnum.ACCOUNT_NOT_EXIST);
        }
        if (!this.passwordEncoder.matches(password, data.getPassword())) {
            throw new BizException(BizCodeEnum.PASSWORD_ERROR);
        }
        return getUserDetailVO(data);
    }

    /**
     * 通过刷新token，生成访问token和刷新token.
     *
     * @param refreshToken 刷新token
     * @return AccessTokenVO
     */
    public AccessTokenVO getAccessTokenByRefreshToken(String refreshToken) {
        String keyPrefix = getKeyPrefix(ShiroConstant.REFRESH_TOKEN_PREFIX);
        String refreshTokenKey = new StringBuilder(keyPrefix).append(refreshToken).toString();
        Object obj = this.redisTemplate.opsForValue().get(refreshTokenKey);
        if (Objects.isNull(obj)) {
            throw new BizException(BizCodeEnum.INVALID_REFRESH_TOKEN);
        }
        ShiroUserVO data = (ShiroUserVO) obj;
        return getUserDetailVO(data);
    }

    /**
     * 退出.
     *
     * @param accessToken 访问token
     */
    public void logout(String accessToken) {
        this.clearAllCache();
        String accessKeyPrefix = getKeyPrefix(ShiroConstant.ACCESS_TOKEN_PREFIX);
        String accessTokenKey = new StringBuilder(accessKeyPrefix).append(accessToken).toString();
        Object obj = this.redisTemplate.opsForValue().get(accessTokenKey);
        if (Objects.nonNull(obj)) {
            // 删除访问token
            this.redisTemplate.delete(accessTokenKey);
            String username = obj.toString();
            String accessUserInfoKey = new StringBuilder(accessKeyPrefix).append(username).toString();
            if (Objects.nonNull(this.redisTemplate.opsForValue().get(accessUserInfoKey))) {
                this.redisTemplate.delete(accessUserInfoKey);
            }
            // 删除刷新token
            String refreshKeyPrefix = getKeyPrefix(ShiroConstant.REFRESH_TOKEN_PREFIX);
            String refreshUserInfoKey = new StringBuilder(refreshKeyPrefix).append(username).toString();
            Object refreshToken = this.redisTemplate.opsForValue().get(refreshUserInfoKey);
            if (Objects.nonNull(refreshToken)) {
                this.redisTemplate.delete(refreshUserInfoKey);
                String refreshTokenStr = refreshToken.toString();
                String refreshTokenKey = new StringBuilder(refreshKeyPrefix).append(refreshTokenStr).toString();
                if (Objects.nonNull(this.redisTemplate.opsForValue().get(refreshTokenKey))) {
                    this.redisTemplate.delete(refreshTokenKey);
                }
            }
        }
    }

    private AccessTokenVO getUserDetailVO(ShiroUserVO data) {
        AccessTokenVO result = new AccessTokenVO();
        result.setAccessToken(this.getAccessToken(data));
        result.setRefreshToken(this.getRefreshToken(data));
        final BigDecimal bigDecimal = new BigDecimal(this.shiroProperty.getJwt().getAccessTokenExpireTime().getSeconds());
        result.setExpiresIn(bigDecimal.intValue());
        return result;
    }

    /**
     * 生成访问token.
     *
     * @param data 用户信息
     * @return String
     */
    private String getAccessToken(ShiroUserVO data) {
        String accessToken;
        String username = data.getUsername();
        String keyPrefix = getKeyPrefix(ShiroConstant.ACCESS_TOKEN_PREFIX);
        String accessUserInfoKey = new StringBuilder(keyPrefix).append(username).toString();
        Object obj = this.redisTemplate.opsForValue().get(accessUserInfoKey);
        if (Objects.isNull(obj)) {
            // 生成访问token
            accessToken = IdUtil.objectId();
            String accessTokenKey = new StringBuilder(keyPrefix).append(accessToken).toString();
            this.redisTemplate.opsForValue().set(accessUserInfoKey, accessToken, this.shiroProperty.getJwt().getAccessTokenExpireTime());
            this.redisTemplate.opsForValue().set(accessTokenKey, username, this.shiroProperty.getJwt().getAccessTokenExpireTime());
        } else {
            accessToken = obj.toString();
            String accessTokenKey = new StringBuilder(keyPrefix).append(accessToken).toString();
            // 重新更新访问token过期时间
            this.redisTemplate.expire(accessUserInfoKey, this.shiroProperty.getJwt().getAccessTokenExpireTime());
            this.redisTemplate.expire(accessTokenKey, this.shiroProperty.getJwt().getAccessTokenExpireTime());
        }
        return accessToken;
    }

    /**
     * 生成刷新token.
     *
     * @param data 用户信息
     * @return String
     */
    private String getRefreshToken(ShiroUserVO data) {
        String refreshToken;
        String username = data.getUsername();
        String keyPrefix = getKeyPrefix(ShiroConstant.REFRESH_TOKEN_PREFIX);
        String refreshUserInfoKey = new StringBuilder(keyPrefix).append(username).toString();
        Object obj = this.redisTemplate.opsForValue().get(refreshUserInfoKey);
        if (Objects.isNull(obj)) {
            // 生成刷新token
            refreshToken = IdUtil.objectId();
            String refreshTokenKey = new StringBuilder(keyPrefix).append(refreshToken).toString();
            this.redisTemplate.opsForValue().set(refreshUserInfoKey, refreshToken, this.shiroProperty.getJwt().getRefreshTokenExpireTime());
            this.redisTemplate.opsForValue().set(refreshTokenKey, data, this.shiroProperty.getJwt().getRefreshTokenExpireTime());
        } else {
            refreshToken = obj.toString();
            String refreshTokenKey = new StringBuilder(keyPrefix).append(refreshToken).toString();
            // 重新更新刷新token过期时间
            this.redisTemplate.expire(refreshUserInfoKey, this.shiroProperty.getJwt().getRefreshTokenExpireTime());
            this.redisTemplate.expire(refreshTokenKey, this.shiroProperty.getJwt().getRefreshTokenExpireTime());
        }
        return refreshToken;
    }

    /**
     * 拼接key前缀.
     *
     * @param key key
     * @return String
     */
    private String getKeyPrefix(String key) {
        String prefix = new StringBuilder(ShiroConstant.REDIS_PREFIX).append(ShiroConstant.CACHE_KEY_PREFIX).toString();
        return new StringBuilder(prefix).append(key).toString();
    }
}