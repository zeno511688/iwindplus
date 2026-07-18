/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.domain.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 多实例kafka监听注解（支持 ${} / #{}）.
 *
 * @author zengdegui
 * @since 2026/03/21 22:00
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface KafkaMultiListener {

    /**
     * 集群名称（未配置，使用默认集群）.
     *
     * @return String
     */
    String cluster() default "";

    /**
     * 主题名称.
     *
     * @return String[]
     */
    String[] topics();

    /**
     * 消费组名称.
     *
     * @return String
     */
    String group() default "";
}
