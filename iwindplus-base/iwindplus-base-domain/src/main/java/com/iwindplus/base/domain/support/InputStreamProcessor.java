/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.support;

import java.io.InputStream;

/**
 * 统一输入流异常模型.
 *
 * @param <T> 泛型
 * @author zengdegui
 * @since 2026/04/04 11:01
 */
@FunctionalInterface
public interface InputStreamProcessor<T> {

    /**
     * 处理输入流.
     *
     * @param inputStream 输入流
     * @return T
     * @throws Throwable 异常
     */
    T process(InputStream inputStream) throws Throwable;
}
