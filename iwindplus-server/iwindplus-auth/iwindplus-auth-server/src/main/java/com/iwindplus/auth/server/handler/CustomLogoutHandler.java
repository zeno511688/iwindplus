/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.handler;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.auth.domain.enums.AuthModuleEnum;
import com.iwindplus.auth.domain.event.LoginLogEvent;
import com.iwindplus.auth.server.config.property.AuthProperty;
import com.iwindplus.auth.server.config.property.AuthProperty.LogConfig;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.support.WebManager;
import com.iwindplus.log.domain.dto.LoginLogDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.web.authentication.logout.LogoutHandler;

/**
 * 退出处理器.
 *
 * @author zengdegui
 * @since 2024/07/08 22:25
 */
@Slf4j
public record CustomLogoutHandler(OAuth2AuthorizationService authorizationService
    , WebManager webManager
    , AuthProperty authProperty
    , ApplicationEventPublisher publisher) implements LogoutHandler {

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (CharSequenceUtil.isBlank(authHeader)) {
            return;
        }
        String token = CharSequenceUtil.replace(authHeader, CommonConstant.HeaderConstant.BEARER_TYPE, "").trim();
        OAuth2Authorization authorization = this.authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN);
        if (Objects.isNull(authorization)) {
            return;
        }

        OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();
        if (Objects.isNull(accessToken) || CharSequenceUtil.isBlank(accessToken.getToken().getTokenValue())) {
            return;
        }
        // 记录登录日志
        final LogConfig log = authProperty.getLog();
        if (Boolean.TRUE.equals(log.getEnabled()) && Boolean.TRUE.equals(log.getEnabledLogout())) {
            LoginLogDTO entity = CustomAuthenticationSuccessHandler.buildLoginLog(request, accessToken.getToken(), AuthModuleEnum.LOGOUT.getValue(),
                AuthModuleEnum.LOGOUT.getDesc());
            // 日志发布事件
            if (Objects.nonNull(entity)) {
                publisher.publishEvent(new LoginLogEvent(this, entity));
            }
        }
        this.authorizationService.remove(authorization);

        webManager.responseData(response, HttpStatus.OK, ResultVO.success());
    }
}
