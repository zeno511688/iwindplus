/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.infrastructure.handler;

import cn.hutool.core.util.IdUtil;
import java.time.Instant;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

/**
 * 不透明访问令牌生成器.
 * <p>
 * 无条件为ACCESS_TOKEN类型生成不透明令牌（UUID格式）， 不依赖客户端的TokenSettings.accessTokenFormat配置.
 *
 * @author zengdegui
 * @since 2026/09/20
 */
public class OpaqueAccessTokenGenerator implements OAuth2TokenGenerator<OAuth2AccessToken> {

    @Override
    public OAuth2AccessToken generate(OAuth2TokenContext context) {
        if (context.getTokenType() == null
            || !OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            return null;
        }
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(context.getRegisteredClient().getTokenSettings().getAccessTokenTimeToLive());
        String tokenValue = IdUtil.simpleUUID();
        return new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            tokenValue,
            issuedAt,
            expiresAt,
            context.getAuthorizedScopes());
    }
}
