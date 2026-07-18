/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro.filter;

import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.util.HttpsUtil;
import com.iwindplus.base.web.support.WebManager;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.springframework.http.HttpStatus;

/**
 * 登陆过滤，器扩展自FormAuthenticationFilter：增加了针对ajax请求的处理.
 *
 * @author zengdegui
 * @since 2018/9/6
 */
@Slf4j
@RequiredArgsConstructor
public class CustomFormAuthorizationFilter extends FormAuthenticationFilter {

    private final WebManager webManager;

    /**
     * 判断是否允许访问,,返回false,则跳到onAccessDenied处理.
     *
     * @param request     请求
     * @param response    响应
     * @param mappedValue mappedValue
     * @return boolean
     */
    @SneakyThrows
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        // 如果已经登陆，还停留在登陆页面，跳转到登陆成功页面
        Subject subject = getSubject(request, response);
        if (Objects.nonNull(subject) && subject.isAuthenticated() && isLoginRequest(request, response)) {
            // 重定向到成功地址
            super.issueSuccessRedirect(request, response);
            return true;
        }
        // 父类判断是否放行
        return super.isAccessAllowed(request, response, mappedValue);
    }

    /**
     * 表示当访问拒绝时是否已经处理了；如果返回true表示需要继续处理；如果返回false表示该拦截器实例已经处理了，将直接返回即可. onAccessDenied是否执行取决于isAccessAllowed的值，如果返回true则onAccessDenied不会执行；如果返回false，执行onAccessDenied
     * 如果onAccessDenied也返回false，则直接返回，不会进入请求的方法（只有isAccessAllowed和onAccessDenied的情况下）
     *
     * @param request  请求
     * @param response 响应
     * @return boolean
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (isLoginRequest(request, response)) {
            if (isLoginSubmission(request, response)) {
                AuthenticationToken token = this.createToken(request, response);
                try {
                    Subject subject = this.getSubject(request, response);
                    subject.login(token);
                    if (subject.isAuthenticated()) {
                        return this.onLoginSuccess(token, subject, request, response);
                    }
                } catch (AuthenticationException var5) {
                    return this.onLoginFailure(token, var5, request, response);
                }
            }
        } else {
            if (HttpsUtil.isAjaxRequest(httpServletRequest)) {
                final HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
                webManager.responseData(httpServletResponse, httpStatus, ResultVO.error(httpStatus));
            } else {
                saveRequestAndRedirectToLogin(request, response);
            }
        }
        return false;
    }

    /**
     * 登录成功增加ajax支持.
     *
     * @param token    token
     * @param subject  subject
     * @param request  请求
     * @param response 响应
     * @return boolean
     * @throws Exception
     */
    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        // 判断是否是ajax请求
        if (HttpsUtil.isAjaxRequest(httpServletRequest)) {
            final HttpStatus httpStatus = HttpStatus.OK;
            webManager.responseData(httpServletResponse, httpStatus, ResultVO.success());
        } else {
            // 重定向到成功地址
            super.issueSuccessRedirect(request, response);
        }
        return false;
    }

    /**
     * 登录失败增加ajax支持.
     *
     * @param token    token
     * @param e        异常
     * @param request  请求
     * @param response 响应
     * @return boolean
     */
    @SneakyThrows
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        // 判断是否是ajax请求
        if (HttpsUtil.isAjaxRequest(httpServletRequest)) {
            final HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
            webManager.responseData(httpServletResponse, httpStatus, ResultVO.error(httpStatus));
            // 过滤器链停止.
        } else {
            // 重定向到登录地址.
            try {
                super.saveRequestAndRedirectToLogin(request, response);
            } catch (IOException ex) {
                log.error("Login failed", ex);
            }
        }
        return false;
    }
}
