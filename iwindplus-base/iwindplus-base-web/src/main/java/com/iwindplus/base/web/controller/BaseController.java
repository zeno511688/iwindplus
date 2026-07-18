/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web.controller;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant.ApiSignConstant;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.context.HeaderContextHolder;
import com.iwindplus.base.domain.context.UserContextHolder;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.BaseSignVO;
import com.iwindplus.base.domain.vo.UserBaseVO;
import com.iwindplus.base.util.ApiSignUtil;
import com.iwindplus.base.util.HttpsUtil;
import com.iwindplus.base.util.MdcUtil;
import com.iwindplus.base.util.domain.dto.ApiSignVerifyDTO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 基础控制层类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Getter
public class BaseController {

    @Resource
    private HttpServletRequest request;

    @Resource
    private HttpServletResponse response;

    /**
     * 获取request请求参数.
     *
     * @return Map<String, String>
     */
    protected Map<String, String> getParameterMap() {
        return HttpsUtil.getParams(this.request);
    }

    /**
     * 获取登录用户信息.
     *
     * @return UserBaseVO
     */
    protected UserBaseVO getUserInfo() {
        return UserContextHolder.getContext();
    }

    /**
     * 获取请求头信息.
     *
     * @return Map<String, String>
     */
    protected Map<String, String> getHeaderMap() {
        return HeaderContextHolder.getContext();
    }

    /**
     * 获取跟踪唯一标识.
     *
     * @return String
     */
    protected String getTraceId() {
        return MdcUtil.getTraceId();
    }

    /**
     * 获取请求唯一标识.
     *
     * @return String
     */
    protected String getRequestId() {
        return getHeaderMap().get(HeaderConstant.X_REQUESTED_ID);
    }

    /**
     * 获取真实ip.
     *
     * @return String
     */
    protected String getRealId() {
        return getHeaderMap().get(HeaderConstant.REAL_IP);
    }

    /**
     * 简化方法，默认无需 AK/SK.
     *
     * @param request 请求
     * @param timeout 签名超时时间
     */
    protected static void checkSign(HttpServletRequest request, Duration timeout) {
        checkSign(request, timeout, null);
    }

    /**
     * AK/SK 特殊调用.
     *
     * @param request 请求
     * @param entity  签名对象
     */
    protected static void checkSignByAkSk(HttpServletRequest request, BaseSignVO entity) {
        checkSign(request, Duration.ofSeconds(entity.getTimeout()), entity);
    }

    private static void checkSign(HttpServletRequest request, Duration timeout, BaseSignVO akSkEntity) {
        Map<String, String> headerMap = HttpsUtil.getFilteredHeaders(request);

        final String timestamp = headerMap.get(ApiSignConstant.X_TIMESTAMP);
        final String nonce = headerMap.get(ApiSignConstant.X_NONCE);
        final String sign = headerMap.get(ApiSignConstant.X_SIGN);

        String accessKey = Optional.ofNullable(akSkEntity).map(BaseSignVO::getAccessKey)
            .orElse(headerMap.get(ApiSignConstant.X_ACCESS_KEY));
        String secretKey = Optional.ofNullable(akSkEntity).map(BaseSignVO::getSecretKey).orElse(null);
        if (CharSequenceUtil.isBlank(accessKey)) {
            throw new BizException(BizCodeEnum.ACCESS_KEY_NOT_EXIST);
        }
        if (CharSequenceUtil.isBlank(secretKey)) {
            throw new BizException(BizCodeEnum.SECRET_KEY_NOT_EXIST);
        }

        // 获取请求 method、path、params
        final String method = Optional.ofNullable(request.getHeader(ApiSignConstant.X_METHOD))
            .orElse(request.getMethod());
        final String path = Optional.ofNullable(request.getHeader(ApiSignConstant.X_PATH))
            .orElse(request.getServletPath());
        final Map<String, Object> params = HttpsUtil.getRequestAndJsonParams(request);

        ApiSignVerifyDTO dto = ApiSignVerifyDTO.builder()
            .accessKey(accessKey)
            .secretKey(secretKey)
            .timestamp(timestamp)
            .nonce(nonce)
            .path(path)
            .method(method)
            .sign(sign)
            .timeout(timeout)
            .params(params)
            .build();

        if (!ApiSignUtil.verifySign(dto)) {
            throw new BizException(BizCodeEnum.INVALID_SIGN);
        }
    }
}
