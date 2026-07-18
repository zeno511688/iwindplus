/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.annotation;

import com.iwindplus.base.domain.enums.SensitiveTypeEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据脱敏注解（响应接口结果时用）.
 *
 * @author zengdegui
 * @since 2024/11/26 23:53
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Sensitive {

    /**
     * 是否开启（可选，默认：true）.
     *
     * @return boolean
     */
    boolean enabled() default true;

    /**
     * 脱敏类型（必填）.
     *
     * @return SensitiveTypeEnum
     */
    SensitiveTypeEnum type() default SensitiveTypeEnum.CUSTOM;

    /**
     * 脱敏开始位置（可选，默认：2，0：表示从第一位开始）.
     *
     * @return int
     */
    int startInclude() default 2;

    /**
     * 脱敏末尾保留位数（可选，默认：2，0：表示不保留）.
     *
     * @return int
     */
    int endReserve() default 2;
}
