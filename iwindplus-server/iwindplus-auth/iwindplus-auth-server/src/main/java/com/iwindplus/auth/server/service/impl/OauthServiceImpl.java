/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.iwindplus.auth.domain.enums.AuthModuleEnum;
import com.iwindplus.auth.domain.event.LoginLogEvent;
import com.iwindplus.auth.server.config.property.AuthProperty;
import com.iwindplus.auth.server.config.property.AuthProperty.LogConfig;
import com.iwindplus.auth.server.handler.CustomAuthenticationSuccessHandler;
import com.iwindplus.auth.server.service.OauthService;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.log.domain.dto.LoginLogDTO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;

/**
 * 认证业务层接口实现类.
 *
 * @author zengdegui 2024/12/4 14:18
 */
@Service
public class OauthServiceImpl implements OauthService {

    @Resource
    private OAuth2AuthorizationService authorizationService;

    @Resource
    private AuthProperty property;

    @Resource
    private ApplicationEventPublisher publisher;

    @Override
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

        final LogConfig log = property.getLog();
        if (Boolean.TRUE.equals(log.getEnabled()) && Boolean.TRUE.equals(log.getEnabledLogout())) {
            LoginLogDTO entity = CustomAuthenticationSuccessHandler.buildLoginLog(request, accessToken, AuthModuleEnum.LOGOUT.getValue(),
                AuthModuleEnum.LOGOUT.getDesc());
            // 日志发布事件
            if (Objects.nonNull(entity)) {
                publisher.publishEvent(new LoginLogEvent(this, entity));
            }
        }
        this.authorizationService.remove(authorization);
    }
}
