/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.validation;

import cn.hutool.core.lang.Validator;
import com.iwindplus.base.domain.annotation.IdCard;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 身份证校验器.
 *
 * @author zengdegui
 * @since 2025/03/14 23:34
 */
public class IdCardValidator implements ConstraintValidator<IdCard, String> {

    @Override
    public void initialize(IdCard constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        // 空不校验
        if (null == value) {
            return true;
        }
        return Validator.isCitizenId(value);
    }
}
