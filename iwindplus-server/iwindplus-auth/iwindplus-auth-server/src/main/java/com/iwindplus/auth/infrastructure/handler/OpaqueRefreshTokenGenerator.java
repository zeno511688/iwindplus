/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.infrastructure.handler;

import cn.hutool.core.util.IdUtil;
import java.time.Duration;
import java.time.Instant;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

/**
 * 不透明刷新令牌生成器.
 * <p>
 * 无条件为REFRESH_TOKEN类型生成不透明令牌（UUID格式）， 不依赖客户端的TokenSettings配置.
 *
 * @author zengdegui
 * @since 2026/09/20
 */
public class OpaqueRefreshTokenGenerator implements OAuth2TokenGenerator<OAuth2RefreshToken> {

    @Override
    public OAuth2RefreshToken generate(OAuth2TokenContext context) {
        if (context.getTokenType() == null
            || !OAuth2TokenType.REFRESH_TOKEN.equals(context.getTokenType())) {
            return null;
        }
        Instant issuedAt = Instant.now();
        String tokenValue = IdUtil.simpleUUID();
        Duration refreshTokenTimeToLive = context.getRegisteredClient()
            .getTokenSettings().getRefreshTokenTimeToLive();
        if (refreshTokenTimeToLive != null) {
            Instant expiresAt = issuedAt.plus(refreshTokenTimeToLive);
            return new OAuth2RefreshToken(tokenValue, issuedAt, expiresAt);
        }
        return new OAuth2RefreshToken(tokenValue, issuedAt, null);
    }
}
