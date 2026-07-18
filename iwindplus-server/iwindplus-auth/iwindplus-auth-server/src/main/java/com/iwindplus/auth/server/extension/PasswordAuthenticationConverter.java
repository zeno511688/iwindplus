/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.extension;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.auth.domain.constant.AuthConstant;
import com.iwindplus.auth.server.util.Oauth2Util;
import com.iwindplus.base.util.HttpsUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * 密码模式认证参数解析器
 * <p>
 * 解析请求参数中的用户名和密码，并构建相应的身份验证(Authentication)对象
 *
 * @author zengdegui
 * @see org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter
 * @since 2024/05/22
 */
public class PasswordAuthenticationConverter implements AuthenticationConverter {

    @Override
    public Authentication convert(HttpServletRequest request) {
        // 授权类型 (必需)
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        // 如果不是密码授权方式
        if (!CharSequenceUtil.equals(grantType, AuthConstant.GrantTypePasswordConstant.PASSWORD.getValue())) {
            return null;
        }

        // 客户端信息
        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

        // 参数提取验证
        MultiValueMap<String, String> parameters = HttpsUtil.getMultiParams(request);

        Set<String> requestedScopes = getScopes(parameters);

        // 用户名验证(必需)
        String username = parameters.getFirst(OAuth2ParameterNames.USERNAME);
        if (CharSequenceUtil.isBlank(username)) {
            Oauth2Util.throwError(OAuth2ErrorCodes.INVALID_REQUEST,
                OAuth2ParameterNames.USERNAME,
                Oauth2Util.ERROR_URI
            );
        }

        // 密码验证(必需)
        String password = parameters.getFirst(OAuth2ParameterNames.PASSWORD);
        if (CharSequenceUtil.isBlank(password)) {
            Oauth2Util.throwError(
                OAuth2ErrorCodes.INVALID_REQUEST,
                OAuth2ParameterNames.PASSWORD,
                Oauth2Util.ERROR_URI
            );
        }

        // 附加参数
        Map<String, Object> additionalParameters = new HashMap<>(16);
        parameters.forEach((key, value) -> {
            if (!key.equals(OAuth2ParameterNames.GRANT_TYPE)
                && !key.equals(OAuth2ParameterNames.SCOPE)
                && !key.equals(OAuth2ParameterNames.USERNAME)
                && !key.equals(OAuth2ParameterNames.PASSWORD)) {
                additionalParameters.put(key, (value.size() == 1) ? value.get(0) : value.toArray(new String[0]));
            }
        });

        return new PasswordAuthenticationToken(
            clientPrincipal,
            additionalParameters,
            requestedScopes,
            username,
            password
        );
    }

    /**
     * 验证scope.
     *
     * @param parameters parameters
     * @return Set<String>
     */
    @Nullable
    public static Set<String> getScopes(MultiValueMap<String, String> parameters) {
        // 令牌申请访问范围验证 (可选)
        String scope = parameters.getFirst(OAuth2ParameterNames.SCOPE);
        if (StringUtils.hasText(scope) && parameters.get(OAuth2ParameterNames.SCOPE).size() != 1) {
            Oauth2Util.throwError(
                OAuth2ErrorCodes.INVALID_REQUEST,
                OAuth2ParameterNames.SCOPE,
                Oauth2Util.ERROR_URI);
        }
        Set<String> requestedScopes = null;
        if (StringUtils.hasText(scope)) {
            requestedScopes = new HashSet<>(Arrays.asList(StringUtils.delimitedListToStringArray(scope, " ")));
        }
        return requestedScopes;
    }

}
