/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web.filter;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.web.domain.property.FilterProperty;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

/**
 * xss过滤器.
 *
 * @author zengdegui
 * @since 2020/4/19
 */
@Slf4j
public class XssFilter extends OncePerRequestFilter {

    private FilterProperty property;
    private MultipartResolver multipartResolver;

    @Override
    protected void initFilterBean() throws ServletException {
        this.property = SpringUtil.getBean(FilterProperty.class);
        this.multipartResolver = SpringUtil.getBean(MultipartResolver.class);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
        throws ServletException, IOException {
        final String charsetName = StandardCharsets.UTF_8.name();
        httpServletRequest.setCharacterEncoding(charsetName);
        httpServletResponse.setCharacterEncoding(charsetName);
        final String contentType = httpServletRequest.getContentType();
        MultipartHttpServletRequest multipartHttpServletRequest = null;
        if (CharSequenceUtil.isNotBlank(contentType) && contentType.contains(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            multipartHttpServletRequest = this.multipartResolver.resolveMultipart(httpServletRequest);
            multipartHttpServletRequest.setCharacterEncoding(Charset.defaultCharset().name());
            httpServletRequest = multipartHttpServletRequest;
        }

        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(httpServletRequest, this.property);
        try {
            filterChain.doFilter(xssRequest, httpServletResponse);
        } finally {
            if (multipartHttpServletRequest != null) {
                this.multipartResolver.cleanupMultipart(multipartHttpServletRequest);
            }
        }
    }
}
