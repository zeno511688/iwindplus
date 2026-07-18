/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.support.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SystemConstant;
import com.iwindplus.base.redis.support.RedisKeyResolver;
import com.iwindplus.base.util.ExpressionUtil;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.interceptor.KeyGenerator;

/**
 * Redis Key解析器接口实现类（默认）.
 *
 * @author zengdegui
 * @since 2020/6/13
 */
@Slf4j
public class DefaultRedisKeyResolver implements RedisKeyResolver {

    @Override
    public String resolver(JoinPoint joinPoint, KeyGenerator keyGenerator, String[] keys) {
        return this.getKeyGeneratorStr(joinPoint, keyGenerator, keys, null);
    }

    /**
     * 获取Redis Key
     *
     * @param joinPoint    切点
     * @param keyGenerator key生成器
     * @param keys         key数组
     * @param prefix       前缀
     * @return String
     */
    protected String getKeyGeneratorStr(JoinPoint joinPoint, KeyGenerator keyGenerator, String[] keys, String prefix) {
        try {
            return buildKey(joinPoint, keyGenerator, keys, prefix);
        } catch (Exception ex) {
            log.error("Redis Key解析异常", ex);
            return IdUtil.simpleUUID();
        }
    }

    private String buildKey(
        JoinPoint joinPoint,
        KeyGenerator keyGenerator,
        String[] keys,
        String prefix) {

        Object target = joinPoint.getTarget();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] args = joinPoint.getArgs();

        String dynamicPart = null;

        // 1. expression 优先
        if (ArrayUtil.isNotEmpty(args) && ArrayUtil.isNotEmpty(keys)) {
            List<Object> resultList = ExpressionUtil.parse(method, args, keys, Object.class);
            if (CollUtil.isNotEmpty(resultList)) {
                dynamicPart = resultList.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.joining(SymbolConstant.UNDERLINE));
            }
        }

        // 2. keyGenerator fallback
        if (CharSequenceUtil.isBlank(dynamicPart) && keyGenerator != null) {
            Object generatedKey = keyGenerator.generate(target, method, args);
            if (generatedKey != null) {
                dynamicPart = generatedKey.toString();
            }
        }

        // 3. 最终兜底（避免 null key）
        if (CharSequenceUtil.isBlank(dynamicPart)) {
            dynamicPart = SystemConstant.DEFAULT;
        }

        // 4. prefix 规范化
        if (CharSequenceUtil.isNotBlank(prefix)) {
            dynamicPart = String.join(SymbolConstant.UNDERLINE, prefix, dynamicPart);
        }

        return dynamicPart;
    }
}