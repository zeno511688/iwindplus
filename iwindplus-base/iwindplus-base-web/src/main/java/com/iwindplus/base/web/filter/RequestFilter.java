/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web.filter;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
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
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 请求过滤器.
 *
 * @author zengdegui
 * @since 2020/4/19
 */
@Slf4j
@RequiredArgsConstructor
public class RequestFilter extends OncePerRequestFilter {

    private final FilterProperty property;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        setDefaultCharset(request, response);

        Map<String, String> headers = HttpsUtil.getFilteredHeaders(request);
        if (headers == null || headers.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            buildRequestedId(headers);
            buildLanguage(headers);
            buildRealIp(headers, request);
            buildUserInfo(headers);
            buildTccInfo(headers);

            HeaderContextHolder.setContext(headers);
            filterChain.doFilter(request, response);
        } finally {
            clearContext();
        }
    }

    private void setDefaultCharset(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String charsetName = StandardCharsets.UTF_8.name();

        if (CharSequenceUtil.isBlank(request.getCharacterEncoding())) {
            request.setCharacterEncoding(charsetName);
        }
        if (CharSequenceUtil.isBlank(response.getCharacterEncoding())) {
            response.setCharacterEncoding(charsetName);
        }
    }

    private void buildRequestedId(Map<String, String> headers) {
        String requestedId = getOrPutHeader(headers, HeaderConstant.X_REQUESTED_ID, UUID.randomUUID().toString());
        MDC.put(HeaderConstant.X_REQUESTED_ID, requestedId);
    }

    private void buildLanguage(Map<String, String> headers) {
        String language = getOrPutHeader(headers, HttpHeaders.ACCEPT_LANGUAGE, HttpsUtil.buildDefaultLanguage());
        MDC.put(HttpHeaders.ACCEPT_LANGUAGE, language);
    }

    private void buildRealIp(Map<String, String> headers, HttpServletRequest request) {
        String realIp = getOrPutHeader(headers, HeaderConstant.X_REAL_IP, JakartaServletUtil.getClientIP(request));
        MDC.put(HeaderConstant.X_REAL_IP, realIp);
    }

    private void buildUserInfo(Map<String, String> headers) {
        FilterCryptoConfig crypto = property.getCrypto();
        String userInfoStr = headers.get(HeaderConstant.X_USER_INFO);

        if (CharSequenceUtil.isBlank(userInfoStr)) {
            UserBaseVO defaultUser = UserContextHolder.getDefaultUser();
            UserContextHolder.setContext(defaultUser);

            userInfoStr = CryptoUtil.encrypt(JacksonUtil.toJsonStr(defaultUser), crypto);
            headers.put(HeaderConstant.X_USER_INFO, userInfoStr);

            return;
        }

        String decryptedData = CryptoUtil.decrypt(userInfoStr, crypto);
        UserBaseVO userInfo = JacksonUtil.parseObject(decryptedData, UserBaseVO.class);
        UserContextHolder.setContext(userInfo);
    }

    private void buildTccInfo(Map<String, String> headers) {
        Optional.ofNullable(headers.get(HeaderConstant.X_TCC_XID))
            .filter(CharSequenceUtil::isNotBlank)
            .ifPresent(TccContextHolder::setXid);
    }

    private String getOrPutHeader(Map<String, String> headers, String key, String defaultValue) {
        String value = headers.get(key);
        if (CharSequenceUtil.isBlank(value)) {
            headers.put(key, defaultValue);
            return defaultValue;
        }
        return value;
    }

    private void clearContext() {
        UserContextHolder.remove();
        HeaderContextHolder.remove();
        TccContextHolder.remove();
        MDC.clear();
    }
}