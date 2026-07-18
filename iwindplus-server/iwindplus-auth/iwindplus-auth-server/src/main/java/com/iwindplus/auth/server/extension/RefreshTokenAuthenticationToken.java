package com.iwindplus.auth.server.extension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;
import org.springframework.util.Assert;

/**
 * 刷新令牌模式身份验证令牌.
 *
 * @author zengdegui
 * @since 2025/03/29 00:20
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class RefreshTokenAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    /**
     * 刷新令牌.
     */
    private final String refreshToken;

    /**
     * 令牌申请访问范围.
     */
    private final Set<String> scopes;

    /**
     * 刷新令牌模式身份验证令牌.
     *
     * @param clientPrincipal      the authenticated client principal
     * @param additionalParameters the additional parameters
     * @param scopes               scopes
     * @param refreshToken         refreshToken
     */
    protected RefreshTokenAuthenticationToken(
        Authentication clientPrincipal,
        Map<String, Object> additionalParameters,
        Set<String> scopes,
        String refreshToken) {
        super(AuthorizationGrantType.REFRESH_TOKEN, clientPrincipal, additionalParameters);
        Assert.hasText(refreshToken, "refreshToken cannot be empty");
        this.refreshToken = refreshToken;
        this.scopes = Collections.unmodifiableSet(null != scopes ? new HashSet<>(scopes) : Collections.emptySet());
    }
}
