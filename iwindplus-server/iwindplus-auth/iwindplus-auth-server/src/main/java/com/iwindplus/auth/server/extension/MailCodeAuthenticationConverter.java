/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.extension;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.auth.domain.constant.AuthConstant;
import com.iwindplus.auth.server.util.Oauth2Util;
import com.iwindplus.base.util.HttpsUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.MultiValueMap;

/**
 * 邮箱验证码认证参数转换器
 * <p>
 * 解析请求参数中的邮箱和验证码，并转换成相应的身份验证(Authentication)对象
 *
 * @author zengdegui
 * @see org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter
 * @since 2024/05/22
 */
public class MailCodeAuthenticationConverter implements AuthenticationConverter {

    @Override
    public Authentication convert(HttpServletRequest request) {
        // 授权类型 (必需)
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!CharSequenceUtil.equals(grantType, AuthConstant.GrantTypeMailCodeConstant.MAIL_CODE.getValue())) {
            return null;
        }

        // 客户端信息
        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

        // 参数提取验证
        MultiValueMap<String, String> parameters = HttpsUtil.getMultiParams(request);

        Set<String> requestedScopes = PasswordAuthenticationConverter.getScopes(parameters);

        // 配置编码(必需)
        String code = parameters.getFirst(AuthConstant.GrantTypeMailCodeConstant.CODE);
        if (CharSequenceUtil.isBlank(code)) {
            Oauth2Util.throwError(
                OAuth2ErrorCodes.INVALID_REQUEST,
                AuthConstant.GrantTypeMailCodeConstant.CODE,
                Oauth2Util.ERROR_URI);
        }

        // 邮箱(必需)
        String mail = parameters.getFirst(AuthConstant.GrantTypeMailCodeConstant.MAIL);
        if (CharSequenceUtil.isBlank(mail)) {
            Oauth2Util.throwError(
                OAuth2ErrorCodes.INVALID_REQUEST,
                AuthConstant.GrantTypeMailCodeConstant.MAIL,
                Oauth2Util.ERROR_URI);
        }

        // 验证码(必需)
        String captcha = parameters.getFirst(AuthConstant.GrantTypeSmsCodeConstant.CAPTCHA);
        if (CharSequenceUtil.isBlank(captcha)) {
            Oauth2Util.throwError(
                OAuth2ErrorCodes.INVALID_REQUEST,
                AuthConstant.GrantTypeSmsCodeConstant.CAPTCHA,
                Oauth2Util.ERROR_URI);
        }

        // 附加参数
        Map<String, Object> additionalParameters = new HashMap<>(16);
        parameters.forEach((key, value) -> {
            if (!key.equals(OAuth2ParameterNames.GRANT_TYPE)
                && !key.equals(OAuth2ParameterNames.SCOPE)
                && !key.equals(AuthConstant.GrantTypeMailCodeConstant.MAIL)
                && !key.equals(AuthConstant.GrantTypeMailCodeConstant.CAPTCHA)) {
                additionalParameters.put(key, (value.size() == 1) ? value.get(0) : value.toArray(new String[0]));
            }
        });

        return new MailCodeAuthenticationToken(
            clientPrincipal,
            additionalParameters,
            requestedScopes,
            code,
            mail,
            captcha
        );
    }
}
