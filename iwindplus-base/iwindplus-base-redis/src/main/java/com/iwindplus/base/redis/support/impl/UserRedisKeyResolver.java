/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.support.impl;

import com.iwindplus.base.domain.context.UserContextHolder;
import com.iwindplus.base.domain.vo.UserBaseVO;
import java.util.Objects;
import java.util.Optional;
import org.aspectj.lang.JoinPoint;
import org.springframework.cache.interceptor.KeyGenerator;

/**
 * 限流Key解析器接口实现类（用户级别）.
 *
 * @author zengdegui
 * @since 2020/6/13
 */
public class UserRedisKeyResolver extends DefaultRedisKeyResolver {

    @Override
    public String resolver(JoinPoint joinPoint, KeyGenerator keyGenerator, String[] keys) {
        String prefix = Optional.ofNullable(UserContextHolder.getContext())
            .map(UserBaseVO::getUserId).map(Objects::toString).orElse(null);
        return this.getKeyGeneratorStr(joinPoint, keyGenerator, keys, prefix);
    }

}
