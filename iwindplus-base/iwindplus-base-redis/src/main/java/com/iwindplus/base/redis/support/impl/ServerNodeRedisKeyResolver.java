/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.support.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.cache.interceptor.KeyGenerator;

/**
 * Redis Key解析器接口实现类（Server节点级别）.
 *
 * @author zengdegui
 * @since 2020/6/13
 */
@Slf4j
public class ServerNodeRedisKeyResolver extends DefaultRedisKeyResolver {

    @Override
    public String resolver(JoinPoint joinPoint, KeyGenerator keyGenerator, String[] keys) {
        String data = null;
        try {
            data = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            log.error(ExceptionConstant.UNKNOWN_HOST_EXCEPTION, ex);
        }
        if (CharSequenceUtil.isNotEmpty(data)) {
            data = CharSequenceUtil.removeAll(data, SymbolConstant.POINT);
        }
        return this.getKeyGeneratorStr(joinPoint, keyGenerator, keys, data);
    }

}