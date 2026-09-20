/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.infrastructure.handler;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.auth.infrastructure.model.dto.OauthUserDTO;
import com.iwindplus.auth.infrastructure.support.Oauth2Util;
import com.iwindplus.base.domain.constant.CommonConstant.UserConstant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

/**
 * 不透明令牌内省器.
 * <p>
 * OPAQUE模式下，通过OAuth2AuthorizationService从Redis中查找令牌对应的授权信息，
 * 提取用户信息并返回OAuth2AuthenticatedPrincipal，供资源服务器进行鉴权.
 *
 * @author zengdegui
 * @since 2026/09/20
 */
@Slf4j
public class CustomOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

    private final OAuth2AuthorizationService authorizationService;

    /**
     * 构造方法.
     *
     * @param authorizationService 授权服务
     */
    public CustomOpaqueTokenIntrospector(OAuth2AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        OAuth2Authorization authorization = this.authorizationService
            .findByToken(token, OAuth2TokenType.ACCESS_TOKEN);
        if (Objects.isNull(authorization)) {
            throw new BadOpaqueTokenException("Invalid token");
        }

        // 校验访问令牌是否过期
        OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();
        if (Objects.isNull(accessToken) || accessToken.isExpired()) {
            throw new BadOpaqueTokenException("Token expired");
        }

        // 从授权信息中提取用户信息
        OauthUserDTO oauthUser = Oauth2Util.getUser(authorization);
        if (Objects.isNull(oauthUser)) {
            throw new BadOpaqueTokenException("Unable to extract user info from token");
        }

        // 构建属性（与JWT声明保持一致）
        Map<String, Object> attributes = new HashMap<>();
        if (Objects.nonNull(oauthUser.getUserId())) {
            attributes.put(UserConstant.USER_ID, oauthUser.getUserId());
        }
        if (Objects.nonNull(oauthUser.getOrgId())) {
            attributes.put(UserConstant.ORG_ID, oauthUser.getOrgId());
        }
        if (Objects.nonNull(oauthUser.getJobNumber())) {
            attributes.put(UserConstant.JOB_NUMBER, oauthUser.getJobNumber());
        }
        if (Objects.nonNull(oauthUser.getUsername())) {
            attributes.put(UserConstant.USERNAME, oauthUser.getUsername());
        }
        if (Objects.nonNull(oauthUser.getNickName())) {
            attributes.put(UserConstant.NICK_NAME, oauthUser.getNickName());
        }
        final Set<GrantedAuthority> authorities = oauthUser.getAuthorities();
        if (CollUtil.isNotEmpty(authorities)) {
            Set<String> permissions = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
            if (CollUtil.isNotEmpty(permissions)) {
                attributes.put(UserConstant.PERMISSIONS, permissions);
            }
        }

        // 构建授权列表
        List<GrantedAuthority> grantedAuthorities = CollUtil.isNotEmpty(authorities)
            ? List.copyOf(authorities)
            : List.of();

        String principalName = Objects.nonNull(oauthUser.getUsername())
            ? oauthUser.getUsername()
            : String.valueOf(oauthUser.getUserId());
        return new DefaultOAuth2AuthenticatedPrincipal(principalName, attributes, grantedAuthorities);
    }
}
