/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.application.service;

import cn.hutool.core.util.ObjectUtil;
import com.iwindplus.auth.common.enums.AuthModuleEnum;
import com.iwindplus.auth.infrastructure.configuration.AuthProperty;
import com.iwindplus.auth.infrastructure.configuration.AuthProperty.LogConfig;
import com.iwindplus.auth.infrastructure.handler.CustomAuthenticationSuccessHandler;
import com.iwindplus.auth.infrastructure.model.dto.LoginLogDTO;
import com.iwindplus.auth.infrastructure.model.event.LoginLogEvent;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;

/**
 * 认证业务层接口.
 *
 * @author zengdegui 2024/12/4 14:18
 */
@Service
@RequiredArgsConstructor
public class OauthApplicationService {

    private final OAuth2AuthorizationService authorizationService;
    private final ApplicationEventPublisher publisher;
    private final AuthProperty property;

    /**
     * 登出.
     *
     * @param request 请求
     */
    public void logout(HttpServletRequest request) {
        final String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (ObjectUtil.isEmpty(token)) {
            throw new BizException(BizCodeEnum.TOKEN_NOT_EXIST);
        }
        OAuth2Authorization authorization = this.authorizationService.findByToken(token, OAuth2TokenType.ACCESS_TOKEN);
        if (Objects.isNull(authorization)) {
            return;
        }
        OAuth2AccessToken accessToken = authorization.getAccessToken().getToken();
        Authentication authentication = authorization.getAttribute(Principal.class.getName());

        final LogConfig log = property.getLog();
        if (Boolean.TRUE.equals(log.getEnabled()) && Boolean.TRUE.equals(log.getEnabledLogout())) {
            LoginLogDTO entity = CustomAuthenticationSuccessHandler.buildLoginLog(
                property,
                request, accessToken, authentication,
                AuthModuleEnum.LOGOUT.getValue(), AuthModuleEnum.LOGOUT.getDesc());
            // 日志发布事件
            if (Objects.nonNull(entity)) {
                publisher.publishEvent(new LoginLogEvent(this, entity));
            }
        }
        this.authorizationService.remove(authorization);
    }
}
