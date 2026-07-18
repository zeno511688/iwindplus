/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.handler;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.web.support.WebManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * 未认证处理.
 *
 * @author zengdegui
 * @since 2024/05/22 22:22
 */
@Slf4j
public record UnAuthenticationEntryPoint(WebManager webManager) implements
    AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
        throws IOException, ServletException {
        log.info("UnAuthenticationEntryPoint={}", authException);

        webManager.responseData(response, HttpStatus.UNAUTHORIZED, ResultVO.error(HttpStatus.UNAUTHORIZED));
    }
}