/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.util;

import com.iwindplus.auth.domain.dto.OauthUserDTO;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.util.HttpsUtil;
import com.iwindplus.base.util.JacksonUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.util.Assert;

/**
 * OAuth2 Endpoint 工具类
 *
 * @author zengdegui
 * @since 2024/05/22 22:22
 */
public class Oauth2Util {

    public static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";

    private Oauth2Util() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 获取 OAuth2ClientAuthenticationToken.
     *
     * @param authentication authentication
     * @return OAuth2ClientAuthenticationToken
     */
    public static OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(Authentication authentication) {
        OAuth2ClientAuthenticationToken clientPrincipal = null;
        if (OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication.getPrincipal().getClass())) {
            clientPrincipal = (OAuth2ClientAuthenticationToken) authentication.getPrincipal();
        }
        return clientPrincipal;
    }

    /**
     * 验证 OAuth2Authorization是否无效.
     *
     * @param authorization authorization
     * @param token         token
     * @param <T>
     * @return <T extends OAuth2Token>
     */
    public static <T extends OAuth2Token> OAuth2Authorization invalidate(
        OAuth2Authorization authorization, T token) {

        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.from(authorization)
            .token(token,
                metadata -> metadata.put(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME, true));

        if (OAuth2RefreshToken.class.isAssignableFrom(token.getClass())) {
            authorizationBuilder.token(
                authorization.getAccessToken().getToken(),
                metadata -> metadata.put(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME, true));

            OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode =
                authorization.getToken(OAuth2AuthorizationCode.class);
            if (null != authorizationCode && !authorizationCode.isInvalidated()) {
                authorizationBuilder.token(
                    authorizationCode.getToken(),
                    metadata -> metadata.put(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME, true));
            }
        }
        return authorizationBuilder.build();
    }

    /**
     * 去除请求参数.
     *
     * @param request    请求
     * @param exclusions 排除
     * @return Map<String, String>
     */
    public static Map<String, String> getParametersIfMatchesAuthorizationCodeGrantRequest(HttpServletRequest request, String... exclusions) {
        if (!matchesAuthorizationCodeGrantRequest(request)) {
            return Collections.emptyMap();
        }
        Map<String, String> parameters = HttpsUtil.getParams(request);
        for (String exclusion : exclusions) {
            parameters.remove(exclusion);
        }
        return parameters;
    }

    /**
     * 判断是否是授权码方式.
     *
     * @param request 请求
     * @return boolean
     */
    public static boolean matchesAuthorizationCodeGrantRequest(HttpServletRequest request) {
        return AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(
            request.getParameter(OAuth2ParameterNames.GRANT_TYPE)) &&
            null != request.getParameter(OAuth2ParameterNames.CODE);
    }

    /**
     * 判断是否pkce方式.
     *
     * @param request 请求
     * @return boolean
     */
    public static boolean matchesPkceTokenRequest(HttpServletRequest request) {
        return matchesAuthorizationCodeGrantRequest(request) &&
            null != request.getParameter(PkceParameterNames.CODE_VERIFIER);
    }

    /**
     * 异常处理.
     *
     * @param errorCode     错误编码
     * @param parameterName 参数名
     * @param errorUri      错误地址
     */
    public static void throwError(String errorCode, String parameterName, String errorUri) {
        String description = String.format("缺少: %s参数", parameterName);
        OAuth2Error error = new OAuth2Error(errorCode, description, errorUri);
        throw new OAuth2AuthenticationException(error);
    }

    /**
     * 验证userCode.
     *
     * @param userCode userCode
     * @return String
     */
    public static String normalizeUserCode(String userCode) {
        Assert.hasText(userCode, "userCode cannot be empty");
        StringBuilder sb = new StringBuilder(userCode.toUpperCase().replaceAll("[^A-Z\\d]+", ""));
        Assert.isTrue(sb.length() == 8, "userCode must be exactly 8 alpha/numeric characters");
        sb.insert(4, '-');
        return sb.toString();
    }

    /**
     * 获取认证用户信息.
     *
     * @return OauthUserDTO
     */
    public static OauthUserDTO getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Oauth2Util.getOauthUser(authentication);
    }

    /**
     * 获取认证用户信息.
     *
     * @param data 授权信息
     * @return OauthUserDTO
     */
    public static OauthUserDTO getUser(OAuth2Authorization data) {
        Authentication authentication = data.getAttribute(Principal.class.getName());
        return Oauth2Util.getOauthUser(authentication);
    }

    @Nullable
    private static OauthUserDTO getOauthUser(Authentication authentication) {
        if (null == authentication) {
            return null;
        }

        final Object principal = authentication.getPrincipal();
        if (principal instanceof OauthUserDTO data) {
            return data;
        }

        if (principal instanceof Map data) {
            return JacksonUtil.parseObject(data, OauthUserDTO.class);
        }
        return null;
    }

}
