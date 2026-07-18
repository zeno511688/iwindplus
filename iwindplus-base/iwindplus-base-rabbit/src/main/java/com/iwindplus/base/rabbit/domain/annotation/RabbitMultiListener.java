/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.domain.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 多实例rabbit监听注解.
 *
 * @author zengdegui
 * @since 2026/03/21 22:00
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RabbitMultiListener {

    /**
     * 集群名称（未配置，使用默认集群）.
     *
     * @return String
     */
    String cluster() default "";

    /**
     * 监听队列.
     *
     * @return String[]
     */
    String[] queues();

    /**
     * 消费组.
     *
     * @return String
     */
    String group() default "";
}
