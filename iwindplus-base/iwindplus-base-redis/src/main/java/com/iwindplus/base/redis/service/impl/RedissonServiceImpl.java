/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.service.impl;

import com.iwindplus.base.redis.operation.RedissonBaseOperation;
import com.iwindplus.base.redis.operation.RedissonIdempotentOperation;
import com.iwindplus.base.redis.operation.RedissonLockOperation;
import com.iwindplus.base.redis.operation.RedissonRateLimiterOperation;
import com.iwindplus.base.redis.operation.RedissonRepeatSubmitOperation;
import com.iwindplus.base.redis.operation.RedissonSerialNumOperation;
import com.iwindplus.base.redis.service.RedissonService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * redis统一调用业务层接口实现类.
 *
 * @author zengdegui
 * @since 2026/05/22 23:20
 */
@Slf4j
public class RedissonServiceImpl implements RedissonService {

    @Resource
    private RedissonBaseOperation redissonBaseOperation;

    @Resource
    private RedissonRepeatSubmitOperation redissonRepeatSubmitOperation;

    @Resource
    private RedissonIdempotentOperation redissonIdempotentOperation;

    @Resource
    private RedissonLockOperation redissonLockOperation;

    @Resource
    private RedissonRateLimiterOperation redissonRateLimiterOperation;

    @Resource
    private RedissonSerialNumOperation redissonSerialNumOperation;

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
