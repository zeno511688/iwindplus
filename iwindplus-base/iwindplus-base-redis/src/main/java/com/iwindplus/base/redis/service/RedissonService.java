/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.service;

import com.iwindplus.base.redis.operation.RedissonBaseOperation;
import com.iwindplus.base.redis.operation.RedissonIdempotentOperation;
import com.iwindplus.base.redis.operation.RedissonLockOperation;
import com.iwindplus.base.redis.operation.RedissonRateLimiterOperation;
import com.iwindplus.base.redis.operation.RedissonRepeatSubmitOperation;
import com.iwindplus.base.redis.operation.RedissonSerialNumOperation;

/**
 * redis统一调用业务层接口.
 *
 * @author zengdegui
 * @since 2026/05/22 23:19
 */
public interface RedissonService {

    /**
     * redis基本操作.
     *
     * @return
     */
    RedissonBaseOperation baseOperation();

    /**
     * redis防重复提交操作.
     *
     * @return
     */
    RedissonRepeatSubmitOperation repeatSubmit();

    /**
     * redis幂等操作.
     *
     * @return
     */
    RedissonIdempotentOperation idempotent();

    /**
     * redis分布式锁操作.
     *
     * @return
     */
    RedissonLockOperation lock();

    /**
     * redis限流操作.
     *
     * @return
     */
    RedissonRateLimiterOperation rateLimiter();

    /**
     * redis生成流水号操作.
     *
     * @return
     */
    RedissonSerialNumOperation serialNum();
}
