/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.operate.domain.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解.
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperateLog {

    /**
     * 是否开启（可选，默认：true）.
     *
     * @return boolean
     */
    boolean enabled() default true;

    /**
     * 键（可选，支持spel表达式）.
     *
     * @return String[]
     */
    String[] keys() default {};

    /**
     * 条件（可选，支持spel表达式）.
     *
     * @return String[]
     */
    String[] conditions() default {};

    /**
     * 业务类型（必填）.
     *
     * @return String
     */
    String bizType();

    /**
     * 操作类型（必填）.
     *
     * @return String
     */
    String operateType();

    /**
     * 操作名称（必填）.
     *
     * @return String
     */
    String operateName();

    /**
     * 操作描述（可选，默认等于操作名称）.
     *
     * @return String
     */
    String operateDesc();
}
