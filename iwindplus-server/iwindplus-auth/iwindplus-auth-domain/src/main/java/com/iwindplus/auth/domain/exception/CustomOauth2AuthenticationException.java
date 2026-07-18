/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.domain.exception;

import com.iwindplus.base.domain.exception.CommonException;
import lombok.Getter;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

/**
 * 自定义oauth2异常
 *
 * @author zengdegui
 * @since 2024/05/22 22:20
 */
@Getter
public class CustomOauth2AuthenticationException extends OAuth2AuthenticationException implements CommonException {

    /**
     * 业务编码.
     */
    private final String bizCode;

    /**
     * 业务信息.
     */
    private final String bizMessage;

    public CustomOauth2AuthenticationException(CommonException code) {
        super(new OAuth2Error(code.getBizCode()), code.getBizMessage());
        this.bizCode = code.getBizCode();
        this.bizMessage = code.getBizMessage();
    }

    public CustomOauth2AuthenticationException(String bizCode, String msg) {
        super(new OAuth2Error(bizCode), msg);
        this.bizCode = bizCode;
        this.bizMessage = msg;
    }
}