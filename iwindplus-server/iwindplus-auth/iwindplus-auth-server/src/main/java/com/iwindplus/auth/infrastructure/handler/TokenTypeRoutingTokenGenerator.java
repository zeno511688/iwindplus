/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.infrastructure.handler;

import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

/**
 * 按令牌类型路由的委托令牌生成器.
 * <p>
 * OPAQUE模式下使用，根据令牌类型路由到对应的生成器：
 * <ul>
 *   <li>ACCESS_TOKEN → 不透明令牌生成器</li>
 *   <li>REFRESH_TOKEN → 刷新令牌生成器</li>
 *   <li>ID_TOKEN → JWT生成器</li>
 * </ul>
 *
 * @author zengdegui
 * @since 2026/09/20
 */
public class TokenTypeRoutingTokenGenerator implements OAuth2TokenGenerator<OAuth2Token> {

    private final OAuth2TokenGenerator<?> accessTokenGenerator;
    private final OAuth2TokenGenerator<?> refreshTokenGenerator;
    private final OAuth2TokenGenerator<?> idTokenGenerator;

    /**
     * 构造方法.
     *
     * @param accessTokenGenerator 访问令牌生成器
     * @param refreshTokenGenerator 刷新令牌生成器
     * @param idTokenGenerator ID令牌生成器
     */
    public TokenTypeRoutingTokenGenerator(
        OAuth2TokenGenerator<?> accessTokenGenerator,
        OAuth2TokenGenerator<?> refreshTokenGenerator,
        OAuth2TokenGenerator<?> idTokenGenerator) {
        this.accessTokenGenerator = accessTokenGenerator;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.idTokenGenerator = idTokenGenerator;
    }

    @Override
    public OAuth2Token generate(OAuth2TokenContext context) {
        if (context.getTokenType() == null) {
            return null;
        }
        if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            return this.accessTokenGenerator.generate(context);
        }
        if (OAuth2TokenType.REFRESH_TOKEN.equals(context.getTokenType())) {
            return this.refreshTokenGenerator.generate(context);
        }
        // ID_TOKEN 及其他类型走JWT生成器
        return this.idTokenGenerator.generate(context);
    }
}
