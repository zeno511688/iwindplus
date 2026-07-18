/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.support;

/**
 * 统一异常模型.
 *
 * @param <T> 泛型
 * @author zengdegui
 * @since 2026/04/04 11:01
 */
@FunctionalInterface
public interface SupplierThrowable<T> {

    /**
     * 获取结果.
     *
     * @return T
     * @throws Throwable
     */
    T get() throws Throwable;
}
