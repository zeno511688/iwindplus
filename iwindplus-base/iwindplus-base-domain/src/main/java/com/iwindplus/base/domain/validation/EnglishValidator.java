/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.validation;

import cn.hutool.core.util.ReUtil;
import com.iwindplus.base.domain.annotation.English;
import com.iwindplus.base.domain.constant.CommonConstant.RegexConstant;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 英文校验器.
 *
 * @author zengdegui
 * @since 2025/03/14 23:34
 */
public class EnglishValidator implements ConstraintValidator<English, String> {

    @Override
    public void initialize(English constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        // 空不校验
        if (null == value) {
            return true;
        }
        return ReUtil.isMatch(RegexConstant.ENGLISH_REGEX, value);
    }
}
