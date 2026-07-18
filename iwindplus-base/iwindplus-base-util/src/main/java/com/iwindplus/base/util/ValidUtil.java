/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * validate校验工具类.
 *
 * @author zengdegui
 * @since 2021/8/2
 */
@Slf4j
public class ValidUtil {

    private ValidUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 校验对象.
     *
     * @param obj       对象
     * @param validator 验证器
     * @param groups    分组
     * @param <T>       泛型
     * @return String
     */
    public static <T> String validateEntity(T obj, Validator validator, Class<?>... groups) {
        Set<ConstraintViolation<T>> validate = validator.validate(obj, groups);
        if (CollUtil.isEmpty(validate)) {
            return null;
        }
        return validate.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(CommonConstant.SymbolConstant.SEMICOLON));
    }
}
