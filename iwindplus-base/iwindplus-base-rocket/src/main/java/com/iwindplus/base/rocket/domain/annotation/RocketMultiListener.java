/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.domain.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 多实例Rocket监听注解.
 *
 * @author zengdegui
 * @since 2026/03/21 22:00
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RocketMultiListener {

    /**
     * 集群名称（未配置，使用默认集群）.
     *
     * @return String
     */
    String cluster() default "";

    /**
     * 消费组
     *
     * @return String
     */
    String group() default "";

    /**
     * 主题名称.
     *
     * @return String
     */
    String topic();

    /**
     * Tag（支持 * 或 多个用 || 分隔）
     *
     * @return String
     */
    String tag() default "*";

    /**
     * 是否顺序消费
     *
     * @return boolean
     */
    boolean orderly() default false;
}
