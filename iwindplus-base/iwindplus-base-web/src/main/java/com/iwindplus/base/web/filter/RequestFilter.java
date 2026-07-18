/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web.filter;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.context.HeaderContextHolder;
import com.iwindplus.base.domain.context.TccContextHolder;
import com.iwindplus.base.domain.context.UserContextHolder;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.util.CryptoUtil;
import com.iwindplus.base.util.HttpsUtil;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.base.web.domain.property.FilterProperty;
import com.iwindplus.base.web.domain.property.FilterProperty.FilterCryptoConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 请求过滤器.
 *
 * @author zengdegui
 * @since 2020/4/19
 */
@Slf4j
public class RequestFilter extends OncePerRequestFilter {

    private FilterProperty property;

    @Override
    protected void initFilterBean() throws ServletException {
        this.property = SpringUtil.getBean(FilterProperty.class);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
        throws ServletException, IOException {
        this.buildDefaultCharset(httpServletRequest, httpServletResponse);

        Map<String, String> headers = HttpsUtil.getFilteredHeaders(httpServletRequest);
        if (MapUtil.isEmpty(headers)) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        try {
            // 设置国际化语言
            this.buildLanguage(headers);

            // 设置真实IP
            this.buildRealIp(httpServletRequest, headers);

            // 设置用户信息
            this.buildUserInfo(headers);

            // tcc信息
            this.buildTccInfo(headers);

            // 请求头信息设置到线程变量中
            HeaderContextHolder.setContext(headers);
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } finally {
            UserContextHolder.remove();
            HeaderContextHolder.remove();
            TccContextHolder.remove();
        }
    }

    private void buildDefaultCharset(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
        throws UnsupportedEncodingException {
        // 设置请求，响应字符集
        final String charsetName = StandardCharsets.UTF_8.name();
        final String requestCharacterEncoding = httpServletRequest.getCharacterEncoding();
        if (CharSequenceUtil.isBlank(requestCharacterEncoding)) {
            httpServletRequest.setCharacterEncoding(charsetName);
        }
        final String responseCharacterEncoding = httpServletResponse.getCharacterEncoding();
        if (CharSequenceUtil.isBlank(responseCharacterEncoding)) {
            httpServletResponse.setCharacterEncoding(charsetName);
        }
    }

    private void buildLanguage(Map<String, String> headers) {
        String language = headers.get(HttpHeaders.ACCEPT_LANGUAGE);
        if (ObjectUtil.isEmpty(language)) {
            headers.put(HttpHeaders.ACCEPT_LANGUAGE, HttpsUtil.buildDefaultLanguage());
        }
    }

    private void buildRealIp(HttpServletRequest httpServletRequest, Map<String, String> headers) {
        String realIp = headers.get(HeaderConstant.REAL_IP);
        if (CharSequenceUtil.isBlank(realIp)) {
            realIp = JakartaServletUtil.getClientIP(httpServletRequest);
            headers.put(HeaderConstant.REAL_IP, realIp);
        }
    }

    private void buildUserInfo(Map<String, String> headers) {
        final FilterCryptoConfig crypto = this.property.getCrypto();
        String userInfoStr = headers.get(HeaderConstant.X_USER_INFO);
        if (CharSequenceUtil.isBlank(userInfoStr)) {
            UserBaseVO userInfo = UserContextHolder.getDefaultUser();
            final String data = JacksonUtil.toJsonStr(userInfo);
            userInfoStr = CryptoUtil.encrypt(data, crypto);
            // 请求头设置用户信息
            headers.put(HeaderConstant.X_USER_INFO, userInfoStr);
        }

        final String data = CryptoUtil.decrypt(userInfoStr, crypto);
        UserBaseVO userInfo = JacksonUtil.parseObject(data, UserBaseVO.class);
        // 用户信息设置到线程变量中
        UserContextHolder.setContext(userInfo);
    }

    private void buildTccInfo(Map<String, String> headers) {
        String xid = headers.get(HeaderConstant.X_TCC_XID);
        if (CharSequenceUtil.isNotBlank(xid)) {
            TccContextHolder.setXid(xid);
        }
    }
}