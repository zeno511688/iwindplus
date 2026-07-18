/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;

/**
 * lambda解析工具.
 *
 * @author zengdegui
 * @since 2026/04/15 22:22
 */
@Slf4j
public class LambdaUtil {

    private static final String METHOD_GET = "get";
    private static final String METHOD_IS = "is";
    private static final String WRITE_REPLACE = "writeReplace";

    private LambdaUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 获取字段名称.
     *
     * @param fn lambda
     * @return 字段名称
     */
    public static <T> String getFieldName(SFunction<T, ?> fn) {
        try {
            Method method = fn.getClass().getDeclaredMethod(WRITE_REPLACE);
            method.setAccessible(true);
            SerializedLambda lambda = (SerializedLambda) method.invoke(fn);

            String methodName = lambda.getImplMethodName();

            if (methodName.startsWith(METHOD_GET)) {
                methodName = methodName.substring(3);
            } else if (methodName.startsWith(METHOD_IS)) {
                methodName = methodName.substring(2);
            }

            return lowerFirst(methodName);
        } catch (Exception e) {
            log.error("解析lambda失败", e);
            throw new BizException(BizCodeEnum.PARSE_ERROR);
        }
    }

    private static String lowerFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
