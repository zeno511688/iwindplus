/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.support.strategy;

import com.baomidou.lock.LockFailureStrategy;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义获取分布式锁异常处理.
 *
 * @author zengdegui
 * @since 2023/11/04 22:17
 */
@Slf4j
public class CustomLockFailureStrategy implements LockFailureStrategy {

    @Override
    public void onLockFailure(String key, Method method, Object[] arguments) {
        log.error("获取锁失败了, key={}, method={}, arguments={}", key, method, arguments);

        throw new BizException(BizCodeEnum.GET_LOCK_ERROR);
    }
}
