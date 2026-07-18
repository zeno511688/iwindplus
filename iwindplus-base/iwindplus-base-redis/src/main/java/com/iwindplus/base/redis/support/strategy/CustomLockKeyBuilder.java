/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.support.strategy;

import cn.hutool.core.util.IdUtil;
import com.baomidou.lock.LockKeyBuilder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.cache.interceptor.KeyGenerator;

/**
 * 自定义分布式锁key生成规则.
 *
 * @author zengdegui
 * @since 2024/07/06 16:45
 */
@Slf4j
public class CustomLockKeyBuilder implements LockKeyBuilder {

    @Resource
    private KeyGenerator keyGenerator;

    @Override
    public String buildKey(MethodInvocation invocation, String[] definitionKeys) {
        try {
            return this.keyGenerator.generate(invocation.getMethod().getDeclaringClass().getName(),
                invocation.getMethod(), invocation.getArguments()).toString();
        } catch (Exception ex) {
            log.warn("自定义分布式锁key异常", ex);
            return IdUtil.simpleUUID();
        }
    }
}
