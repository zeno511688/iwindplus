/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * shiro常数.
 *
 * @author zengdegui
 * @since 2018/12/27
 */
public final class ShiroConstant {
    private ShiroConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 访问token.
     */
    public static final String ACCESS_TOKEN = "access_token";

    /**
     * 刷新token.
     */
    public static final String REFRESH_TOKEN = "refresh_token";

    /**
     * 访问token过期时间.
     */
    public static final String EXPIRES_IN = "expires_in";

    /**
     * redis缓存key前缀.
     */
    public static final String REDIS_PREFIX = "shiro:";

    /**
     * 缓存key前缀.
     */
    public static final String CACHE_KEY_PREFIX = "cache:";

    /**
     * session缓存key前缀.
     */
    public static final String SESSION_KEY_PREFIX = "session:";

    /**
     * 缓存中保存accessToken key的前缀.
     */
    public static final String ACCESS_TOKEN_PREFIX = "access_token:";

    /**
     * 缓存中保存refreshToken key的前缀.
     */
    public static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    /**
     * 认证缓存名.
     */
    public static final String AUTHENTICATION_CACHE_NAME = "authenticationCache";

    /**
     * 鉴权缓存名.
     */
    public static final String AUTHORIZATION_CACHE_NAME = "authorizationCache";

    /**
     * 活动当前session缓存名.
     */
    public static final String ACTIVE_SESSION_CACHE_NAME = "activeSessionCache";
}
