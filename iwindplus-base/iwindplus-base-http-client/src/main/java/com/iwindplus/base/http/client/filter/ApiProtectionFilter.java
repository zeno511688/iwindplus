/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.filter;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.domain.constant.CommonConstant.ApiSignConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.BaseSignExtendVO;
import com.iwindplus.base.http.client.support.ApiProtectionProvider;
import com.iwindplus.base.util.ApiSignUtil;
import com.iwindplus.base.util.domain.dto.ApiSignVerifyDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * API防护过滤器.
 *
 * @author zengdegui
 * @since 2020/4/19
 */
@Slf4j
public class ApiProtectionFilter extends OncePerRequestFilter {

    private ApiProtectionProvider apiProtectionProvider;

    @Override
    protected void initFilterBean() throws ServletException {
        this.apiProtectionProvider = SpringUtil.getBean(ApiProtectionProvider.class);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
        throws ServletException, IOException {

        this.doExecute(httpServletRequest);

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private void doExecute(HttpServletRequest request) {
        final String targetApplication = request.getHeader(ApiSignConstant.APPLICATION);
        if (CharSequenceUtil.isBlank(targetApplication)) {
            return;
        }

        BaseSignExtendVO entity = this.apiProtectionProvider.loadSignCfg(request.getServletPath());
        if (Objects.isNull(entity)) {
            return;
        }

        this.checkSign(request, entity, targetApplication);
    }

    private void checkSign(HttpServletRequest request, BaseSignExtendVO entity, String targetApplication) {
        ApiSignVerifyDTO build = ApiSignVerifyDTO.builder()
            .accessKey(entity.getAccessKey())
            .secretKey(entity.getSecretKey())
            .timestamp(request.getHeader(ApiSignConstant.X_TIMESTAMP))
            .nonce(request.getHeader(ApiSignConstant.X_NONCE))
            .path(Optional.ofNullable(request.getHeader(ApiSignConstant.X_PATH)).orElse(request.getServletPath()))
            .method(Optional.ofNullable(request.getHeader(ApiSignConstant.X_METHOD)).orElse(request.getMethod()))
            .sign(request.getHeader(ApiSignConstant.X_SIGN))
            .timeout(Duration.ofSeconds(entity.getTimeout()))
            .application(targetApplication)
            .build();
        final boolean verifySign = ApiSignUtil.verifySign(build);
        if (!verifySign) {
            throw new BizException(BizCodeEnum.INVALID_SIGN);
        }
    }
}