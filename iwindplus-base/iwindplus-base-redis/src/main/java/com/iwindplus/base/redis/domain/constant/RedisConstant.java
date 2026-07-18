/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * redis常数.
 *
 * @author zengdegui
 * @since 2024/06/10 20:03
 */
public final class RedisConstant {

    private RedisConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 防重复提交前缀 .
     */
    public static final String REPEAT_SUBMIT_KEY_PREFIX = "repeatSubmit:";

    /**
     * 幂等前缀 .
     */
    public static final String IDEMPOTENT_KEY_PREFIX = "idempotent:";

    /**
     * 幂等业务key前缀 .
     */
    public static final String IDEMPOTENT_BIZ_KEY_PREFIX = IDEMPOTENT_KEY_PREFIX + "biz:";

    /**
     * 幂等请求key前缀 .
     */
    public static final String IDEMPOTENT_REQ_KEY_PREFIX = IDEMPOTENT_KEY_PREFIX + "req:";

    /**
     * 幂等频道前缀.
     */
    public static final String IDEMPOTENT_CHANNEL_KEY_PREFIX = IDEMPOTENT_KEY_PREFIX + "channel:";

    /**
     * 锁前缀.
     */
    public static final String LOCK_KEY_PREFIX = "lock:";

    /**
     * 限流前缀.
     */
    public static final String RATE_LIMITER_KEY = "rate_limiter:";
}
