/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.handler;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.auth.domain.constant.AuthConstant;
import com.iwindplus.auth.domain.enums.AuthCodeEnum;
import com.iwindplus.auth.domain.exception.CustomOauth2AuthenticationException;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.exception.CommonException;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.support.WebManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

/**
 * 认证失败处理器
 *
 * @author zengdegui
 * @since 2024/05/22 22:20
 */

@Slf4j
public record CustomAuthenticationFailureHandler(WebManager webManager) implements
    AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
        throws IOException {
        webManager.responseData(response, HttpStatus.OK, ResultVO.error(this.buildException(exception)));
    }

    private CommonException buildException(AuthenticationException exception) {
        if (exception instanceof CustomOauth2AuthenticationException ex) {
            return ex;
        } else if (exception instanceof OAuth2AuthenticationException oAuth2AuthenticationException) {
            String errorCode = oAuth2AuthenticationException.getError().getErrorCode();
            String description = oAuth2AuthenticationException.getError().getDescription();
            switch (errorCode) {
                case OAuth2ErrorCodes.INVALID_CLIENT -> {
                    return this.getInvalidClientError(description);
                }
                case OAuth2ErrorCodes.UNSUPPORTED_GRANT_TYPE -> {
                    return new BizException(AuthCodeEnum.UNSUPPORTED_GRANT_TYPE);
                }
                case OAuth2ErrorCodes.INVALID_REQUEST -> {
                    if (CharSequenceUtil.isBlank(description)) {
                        return new BizException(AuthCodeEnum.INVALID_REQUEST);
                    } else if (description.contains(OAuth2ParameterNames.GRANT_TYPE)) {
                        return new BizException(AuthCodeEnum.GRANT_TYPE_EMPTY);
                    }
                }
                case OAuth2ErrorCodes.INVALID_GRANT -> {
                    return new BizException(AuthCodeEnum.INVALID_GRANT);
                }
                case OAuth2ErrorCodes.INVALID_SCOPE -> {
                    return new BizException(AuthCodeEnum.INVALID_SCOPE);
                }
                case OAuth2ErrorCodes.UNAUTHORIZED_CLIENT -> {
                    return new BizException(AuthCodeEnum.UNAUTHORIZED_CLIENT);
                }
                default -> {
                    return new BizException(AuthCodeEnum.USER_LOGIN_ABNORMAL);
                }
            }
        } else {
            return new BizException(AuthCodeEnum.USER_LOGIN_ABNORMAL);
        }
        return new BizException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private CommonException getInvalidClientError(String description) {
        if (CharSequenceUtil.isBlank(description)) {
            return new BizException(AuthCodeEnum.INVALID_CLIENT);
        } else if (description.contains(OAuth2ParameterNames.CLIENT_ID)) {
            return new BizException(AuthCodeEnum.CLIENT_NOT_EXIST);
        } else if (description.contains(AuthConstant.CLIENT_SECRET_EXPIRES_AT)) {
            return new BizException(AuthCodeEnum.CLIENT_SECRET_EXPIRED);
        } else if (description.contains(OAuth2ParameterNames.CLIENT_SECRET)) {
            return new BizException(AuthCodeEnum.CLIENT_PASSWORD_ERROR);
        } else if (description.contains(AuthConstant.AUTHENTICATION_METHOD)) {
            return new BizException(AuthCodeEnum.AUTHORIZATION_DENIED);
        } else if (description.contains(AuthConstant.CREDENTIALS)) {
            return new BizException(AuthCodeEnum.CLIENT_PASSWORD_EMPTY);
        }
        return new BizException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
