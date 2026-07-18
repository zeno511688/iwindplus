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
 * 第三方绑定授权模式认证参数解析器（用于第三方绑定授权方式，如微信公众号，小程序等）
 * <p>
 * 解析请求参数中的编码，并构建相应的身份验证(Authentication)对象
 *
 * @author zengdegui
 * @see org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter
 * @since 2024/05/22
 */
public class BindCodeAuthenticationConverter implements AuthenticationConverter {

    @Override
    public Authentication convert(HttpServletRequest request) {
        // 授权类型 (必需)
        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        // 如果不是绑定授权方式
        if (!CharSequenceUtil.equals(grantType, AuthConstant.GrantTypeBindCodeConstant.BIND_CODE.getValue())) {
            return null;
        }

        // 客户端信息
        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

        // 参数提取验证
        MultiValueMap<String, String> parameters = HttpsUtil.getMultiParams(request);

        Set<String> requestedScopes = PasswordAuthenticationConverter.getScopes(parameters);

        // 编码(必需)
        String code = parameters.getFirst(AuthConstant.GrantTypeBindCodeConstant.CODE);
        if (CharSequenceUtil.isBlank(code)) {
            Oauth2Util.throwError(
                OAuth2ErrorCodes.INVALID_REQUEST,
                AuthConstant.GrantTypeBindCodeConstant.CODE,
                Oauth2Util.ERROR_URI);
        }

        // 附加参数
        Map<String, Object> additionalParameters = new HashMap<>(16);
        parameters.forEach((key, value) -> {
            if (!key.equals(OAuth2ParameterNames.GRANT_TYPE)
                && !key.equals(OAuth2ParameterNames.SCOPE)
                && !key.equals(AuthConstant.GrantTypeBindCodeConstant.CODE)) {
                additionalParameters.put(key, (value.size() == 1) ? value.get(0) : value.toArray(new String[0]));
            }
        });

        return new BindCodeAuthenticationToken(
            clientPrincipal,
            additionalParameters,
            requestedScopes,
            code
        );
    }
}
