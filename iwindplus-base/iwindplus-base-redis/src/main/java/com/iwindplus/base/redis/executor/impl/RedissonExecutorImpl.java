/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.executor.impl;

import com.iwindplus.base.redis.executor.RedissonExecutor;
import com.iwindplus.base.redis.operation.RedissonBaseOperation;
import com.iwindplus.base.redis.operation.RedissonIdempotentOperation;
import com.iwindplus.base.redis.operation.RedissonLockOperation;
import com.iwindplus.base.redis.operation.RedissonRateLimiterOperation;
import com.iwindplus.base.redis.operation.RedissonRepeatSubmitOperation;
import com.iwindplus.base.redis.operation.RedissonSerialNumOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * redis执行器接口实现类.
 *
 * @author zengdegui
 * @since 2026/05/22 23:20
 */
@Slf4j
@RequiredArgsConstructor
public class RedissonExecutorImpl implements RedissonExecutor {

    private final RedissonBaseOperation redissonBaseOperation;
    private final RedissonRepeatSubmitOperation redissonRepeatSubmitOperation;
    private final RedissonIdempotentOperation redissonIdempotentOperation;
    private final RedissonLockOperation redissonLockOperation;
    private final RedissonRateLimiterOperation redissonRateLimiterOperation;
    private final RedissonSerialNumOperation redissonSerialNumOperation;

    @Override
    public RedissonBaseOperation baseOperation() {
        return redissonBaseOperation;
    }

    @Override
    public RedissonRepeatSubmitOperation repeatSubmit() {
        return redissonRepeatSubmitOperation;
    }

    @Override
    public RedissonIdempotentOperation idempotent() {
        return redissonIdempotentOperation;
    }

    @Override
    public RedissonLockOperation lock() {
        return redissonLockOperation;
    }

    @Override
    public RedissonRateLimiterOperation rateLimiter() {
        return redissonRateLimiterOperation;
    }

    @Override
    public RedissonSerialNumOperation serialNum() {
        return redissonSerialNumOperation;
    }
}
