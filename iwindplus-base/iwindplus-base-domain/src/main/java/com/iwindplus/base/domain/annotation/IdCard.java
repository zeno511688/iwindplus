/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.annotation;

import com.iwindplus.base.domain.validation.IdCardValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 身份证校验注解.
 *
 * @author zengdegui
 * @since 2024/11/26 23:53
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(IdCard.List.class)
@Documented
@Constraint(validatedBy = {IdCardValidator.class})
public @interface IdCard {

    /**
     * 默认错误消息.
     *
     * @return String
     */
    String message() default "身份证号码不合法";

    /**
     * 校验分组.
     *
     * @return Class<?>[]
     */
    Class<?>[] groups() default {};

    /**
     * 负载.
     *
     * @return Class<? extends Payload>[]
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * 指定多个时使用.
     */
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER,
        ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {

        /**
         * 枚举校验注解集合.
         *
         * @return IdCard[]
         */
        IdCard[] value();
    }
}
