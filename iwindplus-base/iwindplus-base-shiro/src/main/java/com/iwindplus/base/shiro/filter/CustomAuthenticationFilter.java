/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro.filter;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.shiro.domain.dto.ShiroTokenDTO;
import com.iwindplus.base.shiro.exception.BizShiroAuthenticationException;
import com.iwindplus.base.web.support.WebManager;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 无状态认证过滤器.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends BasicHttpAuthenticationFilter {

    private final WebManager webManager;

    /**
     * 如果带有 token，则对 token 进行检查，否则直接通过.
     *
     * @param request     请求
     * @param response    响应
     * @param mappedValue mappedValue
     * @return boolean
     * @throws UnauthorizedException
     */
    @SneakyThrows
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws UnauthorizedException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        // 如果请求头存在token,则执行登陆操作,直接返回true
        if (isLoginAttempt(request, response)) {
            //进行Shiro的登录UserRealm
            return executeLogin(request, response);
        } else {
            final HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
            webManager.responseData(httpServletResponse, httpStatus, ResultVO.error(httpStatus));
        }
        return false;
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        final String accessToken = this.getAuthorization(httpServletRequest);
        ShiroTokenDTO shiroTokenDTO = new ShiroTokenDTO(accessToken);
        try {
            Subject subject = getSubject(request, response);
            subject.login(shiroTokenDTO);
            if (subject.isAuthenticated()) {
                return true;
            }
        } catch (BizShiroAuthenticationException ex) {
            final HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
            webManager.responseData(httpServletResponse, httpStatus, ResultVO.error(ex));
        } catch (Exception ex) {
            final HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
            webManager.responseData(httpServletResponse, httpStatus, ResultVO.error(httpStatus));
        }
        return false;
    }

    /**
     * 对跨域提供支持.
     *
     * @param request  请求
     * @param response 响应
     * @return boolean
     * @throws Exception
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        // 跨域时会首先发送一个option请求，这里我们给option请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(httpServletRequest, httpServletResponse);
    }

    @Override
    public boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        return false;
    }

    private String getAuthorization(HttpServletRequest httpServletRequest) {
        final String authorizationParam = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        final String wsAuthorizationParam = httpServletRequest.getHeader(CommonConstant.HeaderConstant.SEC_WEBSOCKET_PROTOCOL);
        final String token = Optional.ofNullable(authorizationParam).orElse(wsAuthorizationParam);
        if (CharSequenceUtil.isNotBlank(token)) {
            return CharSequenceUtil.replace(token, CommonConstant.HeaderConstant.BEARER_TYPE, "").trim();
        }
        return null;
    }
}
