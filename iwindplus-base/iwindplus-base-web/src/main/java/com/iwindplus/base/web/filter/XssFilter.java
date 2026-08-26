/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web.filter;

import com.iwindplus.base.web.domain.property.FilterProperty;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class XssFilter extends OncePerRequestFilter {

    private final FilterProperty property;
    private final MultipartResolver multipartResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        MultipartHttpServletRequest multipartRequest = resolveMultipartRequest(request);
        HttpServletRequest wrappedRequest = multipartRequest != null ? multipartRequest : request;

        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(wrappedRequest, this.property);
        try {
            filterChain.doFilter(xssRequest, response);
        } finally {
            if (multipartRequest != null) {
                this.multipartResolver.cleanupMultipart(multipartRequest);
            }
        }
    }

    private MultipartHttpServletRequest resolveMultipartRequest(HttpServletRequest request) throws IOException {
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            MultipartHttpServletRequest multipartRequest = this.multipartResolver.resolveMultipart(request);
            multipartRequest.setCharacterEncoding(StandardCharsets.UTF_8.name());
            return multipartRequest;
        }
        return null;
    }
}
