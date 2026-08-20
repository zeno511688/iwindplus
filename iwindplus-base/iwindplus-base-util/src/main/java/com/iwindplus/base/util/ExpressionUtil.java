/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.util.ArrayUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SystemConstant;
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
 * SpEL 表达式解析工具类.
 * <p>
 * 提供基于 Spring Expression Language (SpEL) 的表达式解析功能，支持在运行时动态解析方法参数和返回值。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>解析方法参数：通过 #参数名 访问方法入参</li>
 *   <li>解析返回结果：通过 #result 访问方法返回值</li>
 *   <li>支持复杂表达式：支持方法调用、属性访问、运算符等 SpEL 语法</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 解析单个表达式
 * List<String> result = ExpressionUtil.parse(method, args, new String[]{"#user.name"}, String.class);
 *
 * // 解析多个表达式
 * List<Object> results = ExpressionUtil.parse(method, args, result, new String[]{"#order.id", "#order.amount"}, Object.class);
 *
 * // 在注解中使用
 * @OperateLog(bizType = "#order.orderType", operateName = "'处理订单-' + #order.id")
 * public void processOrder(Order order) { ... }
 * }</pre>
 *
 * <h3>SpEL 表达式示例：</h3>
 * <ul>
 *   <li>#user.name - 访问参数 user 的 name 属性</li>
 *   <li>#result.data.id - 访问返回结果的 data 属性的 id 属性</li>
 *   <li>'前缀' + #name + '后缀' - 字符串拼接</li>
 *   <li>#list.size() - 调用方法</li>
 *   <li>#user.age > 18 - 条件判断</li>
 * </ul>
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
     * 解析 SpEL 表达式（仅支持入参）.
     * <p>
     * 解析方法参数相关的 SpEL 表达式，通过 #参数名 访问方法入参。
     * </p>
     *
     * <h3>使用示例：</h3>
     * <pre>{@code
     * // 解析单个参数属性
     * List<String> names = ExpressionUtil.parse(method, args, new String[]{"#user.name"}, String.class);
     *
     * // 解析多个参数属性
     * List<Object> values = ExpressionUtil.parse(method, args, new String[]{"#order.id", "#order.amount"}, Object.class);
     * }</pre>
     *
     * @param method         目标方法
     * @param args           方法参数值数组
     * @param definitionKeys SpEL 表达式数组
     * @param clz            返回结果的目标类型
     * @param <T>            返回结果类型泛型
     * @return 解析结果列表，如果表达式数组为空则返回 null
     */
    public static <T> List<T> parse(Method method, Object[] args, String[] definitionKeys, Class<T> clz) {
        return parse(method, args, null, definitionKeys, clz);
    }

    /**
     * 解析 SpEL 表达式（支持入参和出参）.
     * <p>
     * 解析方法参数和返回值相关的 SpEL 表达式，支持：
     * <ul>
     *   <li>#参数名 - 访问方法入参</li>
     *   <li>#result - 访问方法返回值</li>
     * </ul>
     * </p>
     *
     * <h3>使用示例：</h3>
     * <pre>{@code
     * // 解析入参
     * List<String> names = ExpressionUtil.parse(method, args, null, new String[]{"#user.name"}, String.class);
     *
     * // 解析返回值
     * List<Long> ids = ExpressionUtil.parse(method, args, result, new String[]{"#result.data.id"}, Long.class);
     *
     * // 混合解析入参和返回值
     * List<Object> values = ExpressionUtil.parse(method, args, result,
     *     new String[]{"#order.id", "#result.totalAmount"}, Object.class);
     * }</pre>
     *
     * @param method         目标方法
     * @param args           方法参数值数组
     * @param result         方法返回值（可为 null）
     * @param definitionKeys SpEL 表达式数组
     * @param clz            返回结果的目标类型
     * @param <T>            返回结果类型泛型
     * @return 解析结果列表，如果表达式数组为空则返回 null
     */
    public static <T> List<T> parse(Method method, Object[] args, Object result, String[] definitionKeys, Class<T> clz) {
        if (ArrayUtil.isEmpty(definitionKeys)) {
            return null;
        }
        StandardEvaluationContext context = buildEvaluationContext(method, args, result);
        return getResult(context, definitionKeys, clz);
    }

    /**
     * 构建 SpEL 求值上下文.
     * <p>
     * 创建并配置求值上下文，设置以下变量：
     * <ul>
     *   <li>方法参数变量：#参数名 = 参数值</li>
     *   <li>返回结果变量：#result = 返回值</li>
     * </ul>
     * </p>
     *
     * @param method 目标方法
     * @param args   方法参数值数组
     * @param result 方法返回值（可为 null）
     * @return 配置好的求值上下文
     */
    private static StandardEvaluationContext buildEvaluationContext(Method method, Object[] args, Object result) {
        String[] params = NAME_DISCOVERER.getParameterNames(method);
        StandardEvaluationContext context = new MethodBasedEvaluationContext(null, method, args, NAME_DISCOVERER);
        // 设置入参变量
        if (ArrayUtil.isNotEmpty(params) && ArrayUtil.isNotEmpty(args) && params.length == args.length) {
            IntStream.range(0, params.length).forEach(i -> context.setVariable(params[i], args[i]));
        }
        // 设置返回结果变量
        if (result != null) {
            context.setVariable(SystemConstant.RESULT, result);
        }
        return context;
    }

    /**
     * 批量解析 SpEL 表达式.
     * <p>
     * 使用相同的求值上下文批量解析多个 SpEL 表达式，提高解析效率。
     * </p>
     *
     * @param context        求值上下文
     * @param definitionKeys SpEL 表达式数组
     * @param clz            返回结果的目标类型
     * @param <T>            返回结果类型泛型
     * @return 解析结果列表，顺序与表达式数组顺序一致
     */
    private static <T> List<T> getResult(StandardEvaluationContext context, String[] definitionKeys, Class<T> clz) {
        List<T> definitionKeyList = new ArrayList<>(definitionKeys.length);
        Arrays.stream(definitionKeys).forEach(definitionKey -> {
            try {
                final T key = PARSER.parseExpression(definitionKey).getValue(context, clz);
                definitionKeyList.add(key);
            } catch (Exception ex) {
                log.error("SpEL expression parsing failed: {}", definitionKey, ex);
                definitionKeyList.add(null);
            }
        });
        return definitionKeyList;
    }
}
