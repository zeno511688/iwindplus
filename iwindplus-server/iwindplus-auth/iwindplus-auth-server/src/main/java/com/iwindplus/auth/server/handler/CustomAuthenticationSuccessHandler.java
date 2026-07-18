/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.iwindplus.auth.domain.constant.AuthConstant.GrantTypeBindCodeConstant;
import com.iwindplus.auth.domain.constant.AuthConstant.GrantTypeMailCodeConstant;
import com.iwindplus.auth.domain.constant.AuthConstant.GrantTypePasswordConstant;
import com.iwindplus.auth.domain.constant.AuthConstant.GrantTypeSmsCodeConstant;
import com.iwindplus.auth.domain.enums.AuthModuleEnum;
import com.iwindplus.auth.domain.event.LoginLogEvent;
import com.iwindplus.auth.server.config.property.AuthProperty;
import com.iwindplus.auth.server.config.property.AuthProperty.CookieConfig;
import com.iwindplus.auth.server.config.property.AuthProperty.LogConfig;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.constant.CommonConstant.OauthConstant;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.util.HttpsUtil;
import com.iwindplus.base.web.support.WebManager;
import com.iwindplus.log.domain.dto.LoginLogDTO;
import com.iwindplus.log.domain.dto.LoginLogDTO.LoginLogDTOBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.DefaultOAuth2AccessTokenResponseMapConverter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * 认证成功处理器
 *
 * @author zengdegui
 * @since 2024/05/22 22:20
 */
@Slf4j
public record CustomAuthenticationSuccessHandler(AuthProperty property
    , WebManager webManager
    , ApplicationEventPublisher publisher) implements AuthenticationSuccessHandler {

    private static final Converter<OAuth2AccessTokenResponse, Map<String, Object>> ACCESS_TOKEN_RESPONSE_PARAMETERS_CONVERTER = new DefaultOAuth2AccessTokenResponseMapConverter();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AccessTokenAuthenticationToken accessTokenAuthentication = (OAuth2AccessTokenAuthenticationToken) authentication;

        OAuth2AccessToken accessToken = accessTokenAuthentication.getAccessToken();
        OAuth2RefreshToken refreshToken = accessTokenAuthentication.getRefreshToken();
        Map<String, Object> additionalParameters = accessTokenAuthentication.getAdditionalParameters();

        OAuth2AccessTokenResponse.Builder builder =
            OAuth2AccessTokenResponse.withToken(accessToken.getTokenValue())
                .tokenType(accessToken.getTokenType());
        if (Objects.nonNull(accessToken.getIssuedAt()) && Objects.nonNull(accessToken.getExpiresAt())) {
            builder.expiresIn(ChronoUnit.SECONDS.between(accessToken.getIssuedAt(), accessToken.getExpiresAt()));
        }
        if (Objects.nonNull(refreshToken)) {
            builder.refreshToken(refreshToken.getTokenValue());
        }
        if (CollUtil.isNotEmpty(additionalParameters)) {
            builder.additionalParameters(additionalParameters);
        }
        OAuth2AccessTokenResponse accessTokenResponse = builder.build();
        Map<String, Object> tokenResponseParameters = ACCESS_TOKEN_RESPONSE_PARAMETERS_CONVERTER.convert(accessTokenResponse);

        // 返回响应信息
        ResultVO<Object> result = ResultVO.success(tokenResponseParameters);

        // 存储到cookie
        final ResponseCookie accessTokenCookie = this.buildTokenCookie(OauthConstant.ACCESS_TOKEN, accessToken);
        final ResponseCookie refreshTokenCookie = this.buildTokenCookie(OauthConstant.REFRESH_TOKEN, refreshToken);
        if (accessTokenCookie != null) {
            response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        }
        if (refreshTokenCookie != null) {
            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        }

        webManager.responseData(response, HttpStatus.OK, result);

        // 记录登录日志
        logRecord(request, accessToken);
    }

    private void logRecord(HttpServletRequest request, OAuth2AccessToken accessToken) {
        final LogConfig cfg = property.getLog();
        if (Boolean.FALSE.equals(cfg.getEnabled())) {
            return;
        }

        String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);

        LoginLogDTO entity = null;
        // 登陆日志
        final boolean flag = Boolean.TRUE.equals(cfg.getEnabledLogin())
            && (AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(grantType)
            || AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equals(grantType)
            || AuthorizationGrantType.DEVICE_CODE.getValue().equals(grantType)
            || GrantTypePasswordConstant.PASSWORD.getValue().equals(grantType)
            || GrantTypeSmsCodeConstant.SMS_CODE.getValue().equals(grantType)
            || GrantTypeMailCodeConstant.MAIL_CODE.getValue().equals(grantType)
            || GrantTypeBindCodeConstant.BIND_CODE.getValue().equals(grantType));
        if (flag) {
            entity = CustomAuthenticationSuccessHandler.buildLoginLog(request, accessToken, AuthModuleEnum.LOGIN.getValue(),
                AuthModuleEnum.LOGIN.getDesc());
        }

        // 刷新token日志
        if (Boolean.TRUE.equals(cfg.getEnabledRefreshToken()) && OAuth2ParameterNames.REFRESH_TOKEN.equals(grantType)) {
            entity = CustomAuthenticationSuccessHandler.buildLoginLog(request, accessToken, AuthModuleEnum.REFRESH_TOKEN.getValue(),
                AuthModuleEnum.REFRESH_TOKEN.getDesc());
        }

        if (Objects.nonNull(entity)) {
            // 日志发布事件
            publisher.publishEvent(new LoginLogEvent(this, entity));
        }
    }

    /**
     * LoginLogDTO.
     *
     * @param request     request
     * @param accessToken accessToken
     * @param moduleName  moduleName
     * @param moduleDesc  moduleDesc
     * @return LoginLogDO
     */
    public static LoginLogDTO buildLoginLog(HttpServletRequest request, OAuth2AccessToken accessToken, String moduleName, String moduleDesc) {
        final UserBaseVO data = HttpsUtil.getUserInfo(accessToken.getTokenValue());
        if (Objects.isNull(data)) {
            return null;
        }

        final Long userId = data.getUserId();
        final Long orgId = data.getOrgId();
        if (Objects.isNull(userId) || Objects.isNull(orgId) || userId <= 0L || orgId <= 0L) {
            return null;
        }

        final LoginLogDTOBuilder<?, ?> builder = LoginLogDTO
            .builder()
            .requestId(request.getHeader(HeaderConstant.X_REQUESTED_ID))
            .bizTraceId(MDC.get(HeaderConstant.TRACE_ID))
            .ip(MDC.get(HeaderConstant.REAL_IP))
            .moduleName(moduleName)
            .moduleDesc(moduleDesc)
            .userId(userId)
            .orgId(orgId)
            .createdBy(data.getRealName())
            .createdId(userId)
            .modifiedBy(data.getRealName())
            .modifiedId(userId);

        CustomAuthenticationSuccessHandler.buildUserAgent(builder, request.getHeader(HttpHeaders.USER_AGENT));

        return builder.build();
    }

    private static void buildUserAgent(LoginLogDTOBuilder<?, ?> builder, String userAgentStr) {
        if (CharSequenceUtil.isBlank(userAgentStr)) {
            return;
        }

        UserAgent userAgent = UserAgentUtil.parse(userAgentStr);
        Optional.ofNullable(userAgent).ifPresent(agent ->
            builder.platformName(agent.getPlatform().getName())
                .osName(agent.getOs().getName())
                .browserName(agent.getBrowser().getName())
        );
    }

    private ResponseCookie buildTokenCookie(String name, AbstractOAuth2Token token) {
        if (token == null || token.getTokenValue() == null) {
            return null;
        }

        final CookieConfig cfg = this.property.getCookie();
        if (Boolean.FALSE.equals(cfg.getEnabled())) {
            return null;
        }

        // 兜底保护：防止 issuedAt / expiresAt 为空
        if (token.getIssuedAt() == null || token.getExpiresAt() == null) {
            return null;
        }

        long maxAge = ChronoUnit.SECONDS.between(
            token.getIssuedAt(),
            token.getExpiresAt()
        );

        if (maxAge <= 0) {
            return null;
        }

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie
            .from(name, token.getTokenValue())
            .maxAge(maxAge)
            .path(cfg.getPath())
            .httpOnly(cfg.getHttpOnly())
            .secure(cfg.getSecure())
            .sameSite(cfg.getSameSite());

        if (CharSequenceUtil.isNotBlank(cfg.getDomain())) {
            builder.domain(cfg.getDomain());
        }

        return builder.build();
    }

}
