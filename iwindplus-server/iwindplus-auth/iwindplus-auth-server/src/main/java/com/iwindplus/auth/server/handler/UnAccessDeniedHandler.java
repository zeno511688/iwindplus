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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * 禁止访问处理.
 *
 * @author zengdegui
 * @since 2024/05/22 22:20
 */
@Slf4j
public record UnAccessDeniedHandler(WebManager webManager) implements
    AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
        throws IOException, ServletException {

        webManager.responseData(response, HttpStatus.FORBIDDEN, ResultVO.error(HttpStatus.FORBIDDEN));
    }
}