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
 * 标记 TCC 分支事务.
 *
 * @author zengdegui
 * @since 2026/02/06 20:44
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TccBranchTx {

    /**
     * 上下文路径（协议+服务（ip）+端口）.
     *
     * @return String
     */
    String contextPath() default "";

    /**
     * Confirm 回调地址（相对路径）.
     *
     * @return String
     */
    String confirmUrl();

    /**
     * Cancel 回调地址（相对路径）.
     *
     * @return String
     */
    String cancelUrl();
}
