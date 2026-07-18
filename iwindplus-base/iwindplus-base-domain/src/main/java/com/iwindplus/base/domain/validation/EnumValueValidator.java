/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.validation;

import com.iwindplus.base.domain.annotation.EnumValid;
import com.iwindplus.base.domain.enums.BaseEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 枚举值校验器.
 *
 * @author zengdegui
 * @since 2025/03/14 23:34
 */
public class EnumValueValidator implements ConstraintValidator<EnumValid, Object> {

    /**
     * 注解
     */
    private EnumValid annotation;

    @Override
    public void initialize(EnumValid constraintAnnotation) {
        this.annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        // 空不校验
        if (null == value) {
            return true;
        }
        final Class<? extends BaseEnum<?>> clazz = this.annotation.clazz();
        final List<? extends BaseEnum<?>> list = Arrays.asList(clazz.getEnumConstants());
        return list.stream().anyMatch(data -> Objects.equals(data, value));
    }
}
