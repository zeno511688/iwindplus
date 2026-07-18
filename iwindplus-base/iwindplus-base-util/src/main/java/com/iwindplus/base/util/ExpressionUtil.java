/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.util.ArrayUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * spel 表达式解析工具类.
 *
 * @author zengdegui
 * @since 2024/04/11 22:23
 */
@Slf4j
public class ExpressionUtil {

    /**
     * 用于获取方法参数定义名字.
     */
    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    /**
     * 用于SpEL表达式解析.
     */
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    private ExpressionUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 解析spel表达式.
     *
     * @param method         方法
     * @param args           参数值
     * @param definitionKeys 表达式
     * @param clz            返回结果的类型
     * @param <T>            泛型
     * @return List<T>
     */
    public static <T> List<T> parse(Method method, Object[] args, String[] definitionKeys, Class<T> clz) {
        String[] params = NAME_DISCOVERER.getParameterNames(method);
        if (ArrayUtil.isEmpty(params) || ArrayUtil.isEmpty(args) || ArrayUtil.isEmpty(definitionKeys)) {
            return null;
        }
        if (params.length != args.length) {
            return null;
        }
        StandardEvaluationContext context = new MethodBasedEvaluationContext(null, method, args, NAME_DISCOVERER);
        IntStream.range(0, params.length).forEach(i -> context.setVariable(params[i], args[i]));
        return ExpressionUtil.getResult(context, definitionKeys, clz);
    }

    private static <T> List<T> getResult(StandardEvaluationContext context, String[] definitionKeys, Class<T> clz) {
        List<T> definitionKeyList = new ArrayList<>(definitionKeys.length);
        Arrays.stream(definitionKeys).forEach(definitionKey -> {
            final T key = PARSER.parseExpression(definitionKey).getValue(context, clz);
            definitionKeyList.add(key);
        });
        return definitionKeyList;
    }
}
