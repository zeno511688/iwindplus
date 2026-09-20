/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.infrastructure.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.iwindplus.auth.infrastructure.model.dto.OauthUserDTO;
import com.iwindplus.base.domain.constant.CommonConstant.UserConstant;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

/**
 * 自定义jwt token扩展信息处理器.
 *
 * @author zengdegui
 * @since 2024/07/08 22:25
 */
public class CustomJwtTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {
        JwtClaimsSet.Builder claims = context.getClaims();

        // 客户端模式不返回具体用户信息
        if (AuthorizationGrantType.CLIENT_CREDENTIALS.equals(context.getAuthorizationGrantType())) {
            return;
        }
        OauthUserDTO userDetails = (OauthUserDTO) context.getPrincipal().getPrincipal();
        // 白名单方式写入非敏感字段，避免手机、姓名、邮箱、身份证等敏感信息泄漏到JWT中
        if (ObjectUtil.isNotNull(userDetails.getUserId())) {
            claims.claim(UserConstant.USER_ID, userDetails.getUserId());
        }
        if (ObjectUtil.isNotNull(userDetails.getOrgId())) {
            claims.claim(UserConstant.ORG_ID, userDetails.getOrgId());
        }
        if (ObjectUtil.isNotNull(userDetails.getJobNumber())) {
            claims.claim(UserConstant.JOB_NUMBER, userDetails.getJobNumber());
        }
        if (ObjectUtil.isNotNull(userDetails.getUsername())) {
            claims.claim(UserConstant.USERNAME, userDetails.getUsername());
        }
        if (ObjectUtil.isNotNull(userDetails.getNickName())) {
            claims.claim(UserConstant.NICK_NAME, userDetails.getNickName());
        }
        final Set<GrantedAuthority> authorities = userDetails.getAuthorities();
        if (CollUtil.isNotEmpty(authorities)) {
            Set<String> permissions = AuthorityUtils.authorityListToSet(authorities)
                .stream().filter(Objects::nonNull).sorted().collect(Collectors.toCollection(TreeSet::new));
            if (CollUtil.isNotEmpty(permissions)) {
                claims.claim(UserConstant.PERMISSIONS, permissions);
            }
        }
    }
}