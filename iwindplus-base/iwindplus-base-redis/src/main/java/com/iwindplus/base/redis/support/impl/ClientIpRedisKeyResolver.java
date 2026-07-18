/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.support.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.util.HttpsUtil;
import org.aspectj.lang.JoinPoint;
import org.springframework.cache.interceptor.KeyGenerator;

/**
 * Redis Key解析器接口实现类（客户端IP级别）.
 *
 * @author zengdegui
 * @since 2020/6/13
 */
public class ClientIpRedisKeyResolver extends DefaultRedisKeyResolver {

    @Override
    public String resolver(JoinPoint joinPoint, KeyGenerator keyGenerator, String[] keys) {
        String clientIp = HttpsUtil.getRealIp(HttpsUtil.getHttpServletRequest());
        final String prefix = CharSequenceUtil.isNotBlank(clientIp) ? CharSequenceUtil.removeAll(clientIp, SymbolConstant.POINT) : null;
        return this.getKeyGeneratorStr(joinPoint, keyGenerator, keys, prefix);
    }
}
