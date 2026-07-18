/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.tcc.domain.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 TCC 全局事务.
 *
 * @author zengdegui
 * @since 2026/02/06 20:43
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TccGlobalTx {

    /**
     * 业务类型.
     */
    String bizType();

    /**
     * 超时时间.
     */
    long timeoutSeconds() default 30;
}
